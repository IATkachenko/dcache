package dmg.security.cipher ;
import  dmg.security.cipher.idea.* ;
import  dmg.security.cipher.rsa.* ;
import  java.util.StringTokenizer ;
import  java.io.* ;
/**
  *  
  *
  * @author Patrick Fuhrmann
  * @version 0.1, 15 Feb 1998
  */
public class GenericStreamSecurity implements StreamSecurity {

   private EncryptionKeyContainer _keys = new EncryptionKeyContainer() ;
   
   public GenericStreamSecurity( String keyFile )
          throws IOException {
      
       _keys.readInputStream( new MixedKeyInputStream(
                              new FileInputStream( keyFile ) ) ) ;
			       
   }
   public StreamEncryption getEncryption( String domain )
          throws EncryptionKeyNotFoundException {
      //
      // scan the domain string.
      //
      StringTokenizer st = new StringTokenizer( domain , ":" ) ;
      int tokens = st.countTokens() ;
      if( tokens < 2 )
         throw 
	 new EncryptionKeyNotFoundException( "Invalid domain desc: "+domain);
	 
      String cipher = st.nextToken() ;
      if( cipher.equals( "idea" ) ){
         String name = st.nextToken() ;
	 EncryptionKey key = _keys.get( "shared" , name ) ;
	 try{
	    return new IdeaStreamEncryption( (IdeaEncryptionKey) key ) ;
	 }catch( Exception e ){
	    throw
	    new EncryptionKeyNotFoundException( "not shared : "+name);
	 } 
      }else if( cipher.equals( "rsa" ) ) {
         if( tokens < 3 )
           throw 
	   new EncryptionKeyNotFoundException( "Invalid domain desc: "+domain);
	 String pubName = st.nextToken() ;
	 String priName = st.nextToken() ;
	 EncryptionKey pub = _keys.get( "public"  , pubName ) ;
	 EncryptionKey pri = _keys.get( "private" , priName ) ;
	 try{
	    return new RsaStreamEncryption( (RsaEncryptionKey)pub ,
	                                    (RsaEncryptionKey)pri ) ;
	 }catch( Exception e ){
	    throw
	    new EncryptionKeyNotFoundException( "not rsa : "+domain);
	 } 
      }else
         throw 
	 new EncryptionKeyNotFoundException( "Unknown cipher type : "+cipher );
   }
   public StreamEncryption getSessionEncryption(){
      return new IdeaStreamEncryption() ;
   }
   public StreamEncryption getSessionEncryption( byte [] keyDescriptor )
          throws IllegalEncryptionException {
      return new IdeaStreamEncryption( keyDescriptor ) ;
   }
} 
 
