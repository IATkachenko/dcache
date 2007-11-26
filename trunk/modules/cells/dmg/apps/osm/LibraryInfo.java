package dmg.apps.osm ;

import java.util.* ;
import java.io.* ;
import java.text.* ;

public  class LibraryInfo implements Serializable{
   private String       _name ;
   private Date         _date = new Date() ;
   private int          _now ;
   private DriveInfo [] _drives ;
   private QueueInfo    _queue ;
   private int          _update = 0 ;
   
   LibraryInfo( String name , int now , DriveInfo [] drives , QueueInfo queue ){
      _now    = now ;
      _drives = drives ;
      _queue  = queue ;
   }
   void setUpdate( int update ){ _update = update ; }
   public int getUpdate(){ return _update ; }
   public String toString(){
      StringBuffer sb = new StringBuffer() ;
      if( _queue != null )sb.append( _queue.toString() ).append("\n") ;
      if( _drives != null ){
         for( int i = 0 ; i < _drives.length ; i++ ){
           sb.append( _drives[i].toString() ).append( "\n" ) ;
         }
      }
      return sb.toString() ;
   }
   public DriveInfo [] getDrives(){ return _drives ; }
   public QueueInfo getQueueInfo(){ return _queue ; }
   public boolean equals( Object o ){
     if( ! ( o instanceof LibraryInfo ) )return false ;
     LibraryInfo lib = (LibraryInfo) o ;
     return lib._drives.equals(_drives) && lib._queue.equals(_queue) ;
   }
}
