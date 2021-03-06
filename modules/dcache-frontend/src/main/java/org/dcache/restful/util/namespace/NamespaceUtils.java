package org.dcache.restful.util.namespace;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;

import diskCacheV111.util.AccessLatency;
import diskCacheV111.util.CacheException;
import diskCacheV111.util.FileLocality;
import diskCacheV111.util.PermissionDeniedCacheException;
import diskCacheV111.util.RetentionPolicy;
import diskCacheV111.vehicles.StorageInfo;

import dmg.cells.nucleus.NoRouteToCellException;

import org.dcache.cells.CellStub;
import org.dcache.namespace.FileAttribute;
import org.dcache.namespace.FileType;
import org.dcache.pool.classic.ALRPReplicaStatePolicy;
import org.dcache.pool.classic.ReplicaStatePolicy;
import org.dcache.pool.repository.ReplicaState;
import org.dcache.pool.repository.StickyRecord;
import org.dcache.poolmanager.PoolMonitor;
import org.dcache.restful.providers.JsonFileAttributes;
import org.dcache.restful.qos.QosManagement;
import org.dcache.restful.qos.QosManagementNamespace;
import org.dcache.restful.util.RequestUser;
import org.dcache.vehicles.FileAttributes;

/**
 * <p>Utilities for obtaining and returning file attributes and qos
 *    information.</p>
 */
public final class NamespaceUtils {

    /*
     * FIXME Here the code is assuming the pluggable behaviour of whichever
     * pool a new file lands on.  Currently, pools have a hard-code policy
     * factory (LFSReplicaStatePolicyFactory), which yields two possibilities:
     * VolatileReplicaStatePolicy if lsf is "volatile" or "transient", or
     * ALRPReplicaStatePolicy otherwise.
     *
     * In the following statement, we assume files always land on non-volatile
     * pools.
     */
    private static final ReplicaStatePolicy POOL_POLICY = new ALRPReplicaStatePolicy();

    /**
     * <p>Add quality-of-service attributes (pinned, locality, etc.) </p>
     *
     * @param json               mapped from attributes
     * @param attributes         returned by the query to namespace
     * @param request            to check for client info
     * @param poolMonitor        a PoolMonitor to check locality
     * @param pinmanager         communication with pinmanager
     */
    public static void addQoSAttributes(JsonFileAttributes json,
                                        FileAttributes attributes,
                                        HttpServletRequest request,
                                        PoolMonitor poolMonitor,
                                        CellStub pinmanager)
                    throws CacheException, NoRouteToCellException,
                    InterruptedException {
        if (RequestUser.isAnonymous()) {
            throw new PermissionDeniedCacheException("Permission denied");
        }

        // REVISIT when Resilience can handle all file QoS, remove pinned checks
        boolean isPinnedForQoS = QosManagementNamespace.isPinnedForQoS(attributes,
                                                                       pinmanager);

        FileLocality locality = poolMonitor.getFileLocality(attributes,
                                                        request.getRemoteHost());
        AccessLatency currentAccessLatency
                        = attributes.getAccessLatencyIfPresent().orElse(null);
        RetentionPolicy currentRetentionPolicy
                        = attributes.getRetentionPolicyIfPresent().orElse(null);

        boolean policyIsTape = currentRetentionPolicy == RetentionPolicy.CUSTODIAL;
        boolean latencyIsDisk = currentAccessLatency == AccessLatency.ONLINE
                        || isPinnedForQoS;

        switch (locality) {
            case NEARLINE:
                if (policyIsTape) {
                    json.setCurrentQos(QosManagement.TAPE);
                    if (latencyIsDisk) {
                        /*
                         *  In transition.
                         */
                        json.setTargetQos(QosManagement.DISK_TAPE);
                    }
                } else {
                    /*
                     * not possible according to present
                     * locality definition of NEARLINE; but eventually,
                     * if this happens, something has happened
                     * to the file (could be a REPLICA NEARLINE
                     * file whose only copy has been removed
                     * from the pool).
                     */
                    json.setCurrentQos(QosManagement.UNAVAILABLE);
                }
                break;
            case ONLINE:
                if (latencyIsDisk) {
                    json.setCurrentQos(QosManagement.DISK);
                    if (policyIsTape) {
                        /*
                         *  In transition.
                         */
                        json.setTargetQos(QosManagement.DISK_TAPE);
                    }
                } else {
                    /*
                     *  This is the case where we have found a
                     *  cached file.  Since locality here means
                     *  this cannot be CUSTODIAL, and it is not AL ONLINE,
                     *  it must be REPLICA NEARLINE.  What is the QoS?
                     */
                    // REVISIT this because VOLATILE DOES NOT ACTUALLY MEAN THIS ...
                    json.setCurrentQos(QosManagement.VOLATILE);

                    if (policyIsTape) {
                        /*
                         *  In transition.
                         */
                        json.setTargetQos(QosManagement.TAPE);
                    }
                }
                break;
            case ONLINE_AND_NEARLINE:
                if (latencyIsDisk) {
                    json.setCurrentQos(QosManagement.DISK_TAPE);
                } else {
                    /*
                     *  This is ambiguous.  It could be that the file
                     *  is present on disk, but it is 'unpinned' or has
                     *  an access latency of NEARLINE (a cached replica)
                     *  now, or it could be a transition to a TAPE
                     *  from DISK+TAPE (i.e., is it the current or
                     *  target QoS which is TAPE?).
                     *
                     *  The situation is undecidable. So we leave
                     *  the target Qos blank for the moment.
                     */
                    json.setCurrentQos(QosManagement.TAPE);
                }

                /*
                 * Transitions away from tape are currently forbidden.
                 */
                break;
            case NONE: // NONE implies the target is a directory.
                json.setCurrentQos(directoryQoS(attributes));
                break;
            case UNAVAILABLE:
                json.setCurrentQos(QosManagement.UNAVAILABLE);
                break;

            // LOST is currently not used by dCache
            case LOST:
            default:
                // error cases
                throw new InternalServerErrorException(
                                "Unexpected file locality: " + locality);
        }
    }

    private static String directoryQoS(FileAttributes attributes)
    {
        ReplicaState state = POOL_POLICY.getTargetState(attributes);
        boolean isSticky = POOL_POLICY.getStickyRecords(attributes).stream()
                .anyMatch(StickyRecord::isNonExpiring);
        if (state == ReplicaState.PRECIOUS) {
            return isSticky ? QosManagement.DISK_TAPE : QosManagement.TAPE;
        } else {
            return isSticky ? QosManagement.DISK : QosManagement.VOLATILE;
        }
    }

    /**
     * <p>Map returned attributes to JsonFileAttributes object.</p>
     *
     * @param name                of file
     * @param json                mapped from attributes
     * @param attributes          returned by the query to namespace
     * @param isLocality          used to check weather user queried
     *                            locality of the file
     * @param isLocations         add locations if true
     * @param isOptional          add optional attributes if true
     * @param request             to check for client info
     * @param poolMonitor         for access to remote PoolMonitor
     */
    public static void chimeraToJsonAttributes(String name,
                                               JsonFileAttributes json,
                                               FileAttributes attributes,
                                               boolean isLocality,
                                               boolean isLocations,
                                               boolean isOptional,
                                               HttpServletRequest request,
                                               PoolMonitor poolMonitor) throws CacheException {
        json.setPnfsId(attributes.getPnfsId());

        if (attributes.isDefined(FileAttribute.NLINK)) {
            json.setNlink(attributes.getNlink());
        }

        if (attributes.isDefined(FileAttribute.MODIFICATION_TIME)) {
            json.setMtime(attributes.getModificationTime());
        }

        if (attributes.isDefined(FileAttribute.CREATION_TIME)) {
            json.setCreationTime(attributes.getCreationTime());
        }

        if (attributes.isDefined(FileAttribute.SIZE)) {
            json.setSize(attributes.getSize());
        }

        FileType fileType = null;

        if (attributes.isDefined(FileAttribute.TYPE)) {
            fileType = attributes.getFileType();
            json.setFileType(fileType);
        }

        json.setFileMimeType(name);

        // when user set locality param in the request,
        // the locality should be returned only for directories
        if ((isLocality) && fileType != FileType.DIR) {
            String client = request.getRemoteHost();
            FileLocality fileLocality
                            = poolMonitor.getFileLocality(attributes,
                                                                client);
            json.setFileLocality(fileLocality);
        }

        if (isLocations) {
            if (attributes.isDefined(FileAttribute.LOCATIONS)) {
                json.setLocations(attributes.getLocations());
            }
        }

        if (isOptional) {
            addAllOptionalAttributes(json, attributes);
        }
    }

    /**
     * <p>Adds the rest of the file attributes in the case full
     *    information on the file is requested.</p>
     *
     * @param json                mapped from attributes
     * @param attributes          returned by the query to namespace
     */
    private static void addAllOptionalAttributes(JsonFileAttributes json,
                                                 FileAttributes attributes) {
        if (attributes.isDefined(FileAttribute.ACCESS_LATENCY)) {
            json.setAccessLatency(attributes.getAccessLatency());
        }

        if (attributes.isDefined(FileAttribute.ACL)) {
            json.setAcl(attributes.getAcl());
        }

        if (attributes.isDefined(FileAttribute.ACCESS_TIME)) {
            json.setAtime(attributes.getAccessTime());
        }

        if (attributes.isDefined(FileAttribute.CACHECLASS)) {
            json.setCacheClass(attributes.getCacheClass());
        }

        if (attributes.isDefined(FileAttribute.CHECKSUM)) {
            json.setChecksums(attributes.getChecksums());
        }

        if (attributes.isDefined(FileAttribute.CHANGE_TIME)) {
            json.setCtime(attributes.getChangeTime());
        }

        if (attributes.isDefined(FileAttribute.FLAGS)) {
            json.setFlags(attributes.getFlags());
        }

        if (attributes.isDefined(FileAttribute.OWNER_GROUP)) {
            json.setGroup(attributes.getGroup());
        }

        if (attributes.isDefined(FileAttribute.HSM)) {
            json.setHsm(attributes.getHsm());
        }

        if (attributes.isDefined(FileAttribute.MODE)) {
            json.setMode(attributes.getMode());
        }

        if (attributes.isDefined(FileAttribute.OWNER)) {
            json.setOwner(attributes.getOwner());
        }

        if (attributes.isDefined(FileAttribute.RETENTION_POLICY)) {
            json.setRetentionPolicy(attributes.getRetentionPolicy());
        }

        if (attributes.isDefined(FileAttribute.STORAGECLASS)) {
            json.setStorageClass(attributes.getStorageClass());
        }

        if (attributes.isDefined(FileAttribute.STORAGEINFO)) {
            StorageInfo info = attributes.getStorageInfo();
            json.setStorageInfo(info);
            json.setSuris(info.locations());
        }
    }

    /**
     * Static class, should not be instantiated.
     */
    private NamespaceUtils() {
    }
}
