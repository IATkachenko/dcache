package dmg.util.edb ;

import java.io.* ;

public class JdbmAvElement implements JdbmSerializable {
    private int     _size = 0 ;
    private long    _addr = 0L ;
    public JdbmAvElement(){}
    public JdbmAvElement( long addr , int size ){
       _addr = addr ;
       _size = size ;
    }
    public void writeObject( ObjectOutput out )
           throws java.io.IOException {
       out.writeInt(_size) ;
       out.writeLong(_addr) ;
       return ;   
    }
    public void readObject( ObjectInput in )
           throws java.io.IOException, ClassNotFoundException {
           
           
       _size = in.readInt() ;
       _addr = in.readLong() ;
       
       return ;
    }
    public int getPersistentSize() { return 0 ; }

}
