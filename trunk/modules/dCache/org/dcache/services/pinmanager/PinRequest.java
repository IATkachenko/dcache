package org.dcache.services.pinmanager;

import java.util.TimerTask;

import dmg.cells.nucleus.CellMessage;
import diskCacheV111.vehicles.PinManagerMessage;
import diskCacheV111.vehicles.PinManagerPinMessage;
import diskCacheV111.vehicles.PinManagerUnpinMessage;
import diskCacheV111.vehicles.StorageInfo;
import diskCacheV111.util.PnfsId;

/**
 * Models a pin request. 
 *
 * Since the same file canned be pinned multiple times, we distinguish
 * between the individual requests and the single pin held on the
 * file. 
 */
public class PinRequest  
{
    /** Our ID used to uniquely identify the request. */
    private long _pinRequestId;

    /** The expiration time of the request. */
    private long _expiration;

    /** The ID used by the client to track the request. */
    private long _requestId;

    /**
     * The message envelope of the pin request. May be null if the
     * request was recovered from the database or was done via the
     * administrative interface.
     */
    private CellMessage _cellMessage;

    /**
     * Timer task associated with the request. The intention is that
     * the timer takes care of expiring the request.
     */
    private TimerTask _timer;

    public PinRequest(long pinRequestId, long expiration, long requestId) 
    {
        _pinRequestId = pinRequestId;
        _expiration = expiration;
        _requestId = requestId;
    }

    /**
     * The pin request ID is generated by the PinManager and is unique
     * among all currently existing requests.
     */
    public long getPinRequestId() 
    {
        return _pinRequestId;
    }

    /**
     * The request ID is provided by the client in the initial
     * PinMessage. It has no meaning to the pin manager.
     */
    public long getRequestId() 
    {
        return _requestId;
    }

    /**
     * Returns the expiration time of the request in milliseconds
     * since midnight, January 1, 1970 UTC.
     *
     * @see System.currentTimeMillis
     */
    public long getExpiration() 
    {
        return _expiration;
    }

    /**
     * Sets the expiration time in milliseconds of the request.
     *
     * @see getExpiration
     */
    public void setExpiration(long expiration) 
    {
        _expiration = expiration;
    }

    /**
     * Returns the remaining lifetime in milliseconds of the request.
     * Returns zero when the request has expired.
     */
    public long getRemainingLifetime()
    {
        return Math.max(0, _expiration - System.currentTimeMillis());
    }

    /**
     * Sets the timer task of the request. The timer task is usually
     * used for expiring the request.
     */
    public void setTimer(TimerTask task)
    {
        _timer = task;
    }

    /**
     * Returns the current timer task, if any.
     * 
     * @see setTimer.
     */
    public TimerTask getTimer()
    {
        return _timer;
    }
        
    /**
     * Returns status information about the request.
     */
    public String toString() 
    {
        PinManagerPinMessage request = getRequest();
        StringBuffer sb = new StringBuffer();
        sb.append("request id : ");
        sb.append(_pinRequestId);
        sb.append(" expires : ").append(new java.util.Date(_expiration));
        sb.append(" orginal srm request id:").append(_requestId);
        if (request != null) {
            sb.append(" request message : ").append(request);
        }
        return sb.toString();
    }
                
    /**
     * Returns the PinManagerPinMessage that triggered the creation of
     * this request. Returns null if this message is no longer available.
     */
    public PinManagerPinMessage getRequest() 
    {
        if (_cellMessage == null) {
            return null;
        }
        return (PinManagerPinMessage)_cellMessage.getMessageObject();
    }
        
    /**
     * Sets the message envelope for the PinManagerPinMessage that
     * triggered the creation of this request. As a side effect, the
     * pin request ID is set in the message structure.
     * 
     * @throws ClassCastException if the message object is not a
     *                            PinManagerPinMessage
     */
    public void setCellMessage(CellMessage cellMessage) 
    {
        _cellMessage = cellMessage;
        if (_cellMessage != null) {
            PinManagerPinMessage msg = 
                (PinManagerPinMessage)_cellMessage.getMessageObject();
            msg.setPinId(Long.toString(getPinRequestId()));
        }
    }

    public CellMessage getCellMessage() 
    {
        return _cellMessage;
    }
}
