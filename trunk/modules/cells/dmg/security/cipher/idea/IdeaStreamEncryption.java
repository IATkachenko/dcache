package dmg.security.cipher.idea ;
import  dmg.security.cipher.* ;
import  java.util.Random ;
import  java.util.Date ;

/**
  *  
  *
  * @author Patrick Fuhrmann
  * @version 0.1, 15 Feb 1998
  */
public class      IdeaStreamEncryption 
       implements StreamEncryption {

  Idea _en , _de ;
  
  public IdeaStreamEncryption( IdeaEncryptionKey key ){
     _IdeaStreamEncryption( key.getBytes() ) ;
  }
  public IdeaStreamEncryption( byte [] keyDesc )
         throws IllegalEncryptionException {
         
         
      int keyDescLength = 2 + 16 + 8 ;
      if( keyDesc.length < keyDescLength )
        throw new IllegalEncryptionException( "Desc too short" ) ;
      if( ( keyDesc[0] != 1 ) || ( keyDesc[1] != 24) )
        throw new IllegalEncryptionException( "Desc : wrong prot." ) ;
        
      byte [] key   = new byte[16] ;
      byte [] IV    = new byte[8] ;
      
      System.arraycopy( keyDesc , 2 , key , 0 , 16 ) ;
      System.arraycopy( keyDesc , 18 , IV , 0 , 8 ) ;
      
      _IdeaStreamEncryption( key ) ;
      setStartValue( IV ) ;
      
  }
  public IdeaStreamEncryption(){
     Random r = new Random( new Date().getTime() ) ;
     byte [] key = new byte[16] ;
     r.nextBytes( key ) ;
     System.out.print( " IdeaStreamEncryption : choose Key : " ) ;
     for( int i = 0 ; i < key.length ; i++ )System.out.print( " "+key[i] ) ;   
     System.out.println("");  
     _en  = new Idea( key ) ;
     _de  = new Idea( key ) ;
  }
  private void  _IdeaStreamEncryption( byte [] key ){
     _en  = new Idea( key ) ;
     _de  = new Idea( key ) ;
     System.out.print( " IdeaStreamEncryption : setKey : " ) ;
     for( int i = 0 ; i < key.length ; i++ )System.out.print( " "+key[i] ) ;   
     System.out.println("");  
  }
  public byte [] getKeyDescriptor(){ 
     byte [] IV  = getStartValue() ;
     byte [] key = getKeyBytes() ;
     
     int keyDescLength = IV.length + key.length + 2 ;
     
     byte [] keyDesc = new byte [ keyDescLength ] ;
     keyDesc[0] = (byte)1 ;
     keyDesc[1] = (byte)(IV.length + key.length) ;
     
     System.arraycopy( key  , 0 , keyDesc , 2 , key.length ) ;
     System.arraycopy( IV   , 0 , keyDesc , 18 , IV.length ) ;
     
     return keyDesc ;
  
  }
  public byte [] encrypt( byte [] plain , int off , int size ) {
      byte [] res = new byte[ size ] ;
      _en.encryptCFB64( plain , off , res , 0 , size ) ;
      return res ;
  
  }
  public byte [] decrypt( byte [] cypher , int off , int size  ) {
      byte [] res = new byte[ size ] ;
      _de.decryptCFB64( cypher , off , res , 0 , cypher.length ) ;
      return res ;
  
  }
  public void encrypt( byte [] plain , int plainOff ,
                       byte [] cypher , int cypherOff , int len ){

      _en.encryptCFB64( plain  , plainOff ,
                        cypher , cypherOff , len ) ;
     
  }
  public void decrypt( byte [] cypher , int cypherOff ,
                       byte [] plain , int plainOff , int len ){

      _de.decryptCFB64( cypher  , cypherOff ,
                        plain   , plainOff  , len ) ;
  }
  public boolean canOneToOne() { return true ; }
  public int     getMaxBlockSize(){ return 0 ; }
  public byte [] getStartValue(){ return _en.getStartValue() ; }
  public byte [] getKeyBytes(){   return _en.getKeyBytes() ;   }
  public void setStartValue( byte [] start ){ 
     _en.setStartValue( start ) ;
     _de.setStartValue( start ) ;
     return ;
  }
}
