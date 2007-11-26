//package diskCacheV111.services.authorization.gplazmalite.storageauthzdbService;
package gplazma.gplazmalite.storageauthzdbService;

import java.util.*;

public class StoragePasswordRecord extends StorageAuthorizationBase
{
    String Password = null;

	public StoragePasswordRecord(String user,
					     String passwd, boolean readOnly,
                         int priority, int uid,int gid,
                         String home,String root,String fsroot)
	{
        this(user, passwd, readOnly, priority, uid, gid, home, root, fsroot,false);
	}
    
    public StoragePasswordRecord(String user,
                         String passwd, boolean readOnly, 
                         int priority, int uid,int gid,
                         String home,String root,String fsroot,
                         boolean isPlain)
    {
        super(user, readOnly, priority, uid, gid, home, root, fsroot);
        
        if(isPlain)
        {
            setPassword(passwd);
        }
        else
        {
            Password = passwd;
        }
    }


 	public String serialize()
 	{
	    
 		String str = Username + " " +
 			Password + " " +
 		  readOnlyStr() + " " +
 			priority + " " +
      UID + " " +
 			GID + " " +
 			Home + " " +
 			Root;
 		if ( ! Root.equals(FsRoot) )
 			str = str + " " + FsRoot;	
 		return str;
 	}

   public String toString()
    {
        return serialize();
    }

    public String toDetailedString()
    {
        StringBuffer stringbuffer = new StringBuffer(" User Password Record for ");
        stringbuffer.append(Username).append(" :\n");
        stringbuffer.append("  Password Hash = ").append(Password).append('\n');
		    stringbuffer.append("      read-only = " + readOnlyStr() + "\n");
        stringbuffer.append("       priority = ").append(priority).append('\n');
        stringbuffer.append("            UID = ").append(UID).append('\n');
        stringbuffer.append("            GID = ").append(GID).append('\n');
        stringbuffer.append("           Home = ").append(Home).append('\n');
        stringbuffer.append("           Root = ").append(Root).append('\n');
        stringbuffer.append("         FsRoot = ").append(FsRoot).append('\n');
        return stringbuffer.toString();
    }

	
	public String hashPassword(String pwd)
	{
		String uandp = "1234567890" + Username + " " + pwd;
		return java.lang.Integer.toHexString(uandp.hashCode());
	}

	public void setPassword(String pwd)
	{
		if( pwd.equals("-") )
			Password = "-";
		else
			Password = hashPassword(pwd);
	}
	
	public void disable()
	{
		Password = "#";
	}

	public boolean passwordIsValid(String clear_pwd)
	{
		return Password.equals(hashPassword(clear_pwd));
	}
	
	public boolean isDisabled()
	{
		return Password.equals("#");
	}
	
	public boolean isAnonymous()
	{
		return Password.equals("-");
	}

        public boolean isWeak() { return true; }

	public boolean isValid()
	{
		return Username != null &&
			Password != null;
	}
}

