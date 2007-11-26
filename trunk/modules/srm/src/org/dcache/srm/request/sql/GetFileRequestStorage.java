/*
 * GetFileRequestStorage.java
 *
 * Created on June 17, 2004, 4:49 PM
 */

package org.dcache.srm.request.sql;
import java.sql.*;
import org.dcache.srm.request.FileRequest;
import org.dcache.srm.request.GetFileRequest;
import org.dcache.srm.util.Configuration;
import org.dcache.srm.scheduler.State;
import org.dcache.srm.scheduler.Job;

/**
 *
 * @author  timur
 */
public class GetFileRequestStorage extends DatabaseFileRequestStorage {
    
    /** Creates a new instance of GetFileRequestStorage */
    public GetFileRequestStorage(    
    Configuration configuration
    )  throws SQLException {
        super(configuration);
    }
   
        
    public void say(String s){
        if(logger != null) {
           logger.log(" GetFileRequestStorage: "+s);
        }
    }
    
    public void esay(String s){
        if(logger != null) {
           logger.elog(" GetFileRequestStorage: "+s);
        }
    }
    
    public void esay(Throwable t){
        if(logger != null) {
           logger.elog(t);
        }
    }

    
    protected FileRequest getFileRequest(Connection _con,
        Long ID, 
        Long NEXTJOBID, 
        long CREATIONTIME, 
        long LIFETIME, 
        int STATE, 
        String ERRORMESSAGE, 
        String CREATORID, 
        String SCHEDULERID, 
        long SCHEDULER_TIMESTAMP, 
        int NUMOFRETR, 
        int MAXNUMOFRETR, 
        long LASTSTATETRANSITIONTIME, 
        Long REQUESTID, 
        Long CREDENTIALID, 
        String STATUSCODE,
        java.sql.ResultSet set, 
        int next_index) throws SQLException {
           String SURL = set.getString(next_index++);
           String TURL = set.getString(next_index++);
           String FILEID = set.getString(next_index++);
           String PINID = set.getString(next_index);
            Job.JobHistory[] jobHistoryArray = 
            getJobHistory(ID,_con);
           return new GetFileRequest(
            ID,
            NEXTJOBID ,
            this,
            CREATIONTIME,
            LIFETIME,
            STATE,
            ERRORMESSAGE,
            CREATORID,
            SCHEDULERID,
            SCHEDULER_TIMESTAMP,
            NUMOFRETR,
            MAXNUMOFRETR,
            LASTSTATETRANSITIONTIME,
            jobHistoryArray,
            REQUESTID,
            CREDENTIALID,
            STATUSCODE,
            configuration,
            SURL,
            TURL,
            FILEID,
            PINID
            );
    }
    
    public String getFileRequestCreateTableFields() {
        return                     
        ","+
        "SURL "+  stringType+
        ","+
        "TURL "+  stringType+
        ","+
        "FILEID "+  stringType+
        ","+
        "PINID "+  stringType;
    }
    
    private static int ADDITIONAL_FIELDS = 4;

    public static final String TABLE_NAME = "getfilerequests";
    public String getTableName() {
        return TABLE_NAME;
    }
    
    public void getUpdateAssignements(FileRequest fr,StringBuffer sb) {
        if(fr == null || !(fr instanceof GetFileRequest)) {
            throw new IllegalArgumentException("fr is not GetFileRequest" );
        }
        GetFileRequest gfr = (GetFileRequest)fr;
         sb.append(", SURL = '").append(gfr.getSurlString()).append("',");
        String tmp =gfr.getTurlString();
        if(tmp == null) {
            sb.append(" TURL =NULL, ");
        }
        else {
            sb.append("TURL = '").append(tmp).append("', ");
        }
        
        tmp =gfr.getFileId();
        if(tmp == null) {
            sb.append(" FILEID =NULL, ");
        }
        else {
            sb.append("FILEID = '").append(tmp).append("', ");
        }
        tmp =gfr.getPinId();
        if(tmp == null) {
            sb.append(" PINID =NULL ");
        } 
        else {
            sb.append("PINID = '").append(tmp).append("' ");
        }
    }
    
     public void getCreateList(FileRequest fr,StringBuffer sb) {
        if(fr == null || !(fr instanceof GetFileRequest)) {
            throw new IllegalArgumentException("fr is not GetFileRequest" );
        }
        GetFileRequest gfr = (GetFileRequest)fr;
        sb.append(", '").append(gfr.getSurlString()).append("', ");
        String tmp = gfr.getTurlString();
        if(tmp == null) {
            sb.append("NULL, ");
        }
        else {
            sb.append('\'').append(tmp).append("', ");
        }
        tmp =gfr.getFileId();
        if(tmp == null) {
            sb.append("NULL, ");
        }
        else {
            sb.append('\'').append(tmp).append("', ");
        }
        tmp = gfr.getPinId();
        if(tmp == null) {
            sb.append("NULL ");
        }
        else {
            sb.append('\'').append(tmp).append("' ");
        }
    }
   
     public String getRequestTableName() {
         return GetRequestStorage.TABLE_NAME;
     }     
     
     protected void __verify(int nextIndex, int columnIndex, String tableName, String columnName, int columnType) throws SQLException {
         /*
          *       "SURL "+  stringType+
        ","+
        "TURL "+  stringType+
        ","+
        "FILEID "+  stringType+
        ","+
        "PINID "+  stringType;
         */
        if(columnIndex == nextIndex) {
            verifyStringType("SURL",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+1)
        {
            verifyStringType("TURL",columnIndex,tableName, columnName, columnType);
            
        }
        else if(columnIndex == nextIndex+2)
        {
            verifyStringType("FILEID",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+3)
        {
            verifyStringType("PINID",columnIndex,tableName, columnName, columnType);
        }
        else {
            throw new SQLException("database table schema changed:"+
                    "table named "+tableName+
                    " column #"+columnIndex+" has name \""+columnName+
                    "\"  has type \""+getTypeName(columnType)+
                    " this column should not be present!!!");
        }
     }
     
    protected int getMoreCollumnsNum() {
         return ADDITIONAL_FIELDS;
     }
     
}
