package hmgapi.mods.projection;

import java.io.*;
import java.security.MessageDigest;

/** Copies only the pinned helper into the owning app's private directory. */
final class NativePayload {
    static final String SHA256="1709fa3536888258ed6c1154c580e475149281f1e1d8686d28471fa1efb6b6f8";
    private NativePayload() {}
    static byte[] read(File source) throws Exception {
        long size=source.length();
        if(size<52 || size>1048576)throw new IOException("invalid helper size");
        ByteArrayOutputStream out=new ByteArrayOutputStream((int)size);
        try(InputStream in=new FileInputStream(source)) {
            byte[] b=new byte[8192];int n;
            while((n=in.read(b))!=-1) {
                if(out.size()+n>1048576)throw new IOException("helper exceeds size limit");
                out.write(b,0,n);
            }
        }
        byte[] bytes=out.toByteArray();verify(bytes);return bytes;
    }
    static void verify(byte[] bytes) throws Exception {
        if(bytes==null || bytes.length<52 || bytes.length>1048576)throw new IOException("missing helper payload");
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder text=new StringBuilder();for(byte b:hash)text.append(String.format("%02x",b&255));
        if(!SHA256.equals(text.toString()))throw new IOException("helper hash mismatch");
    }
    static File stage(File privateDir,byte[] bytes) throws Exception {
        verify(bytes);
        File target=new File(privateDir,"libstockgraph-"+SHA256.substring(0,16)+".so");
        if(target.isFile()) {
            try {read(target);return target;}catch(IOException invalid) { /* Replace a stale/corrupt copy. */ }
        }
        File temp=File.createTempFile("stockgraph-",".part",privateDir);
        try {
            try(FileOutputStream out=new FileOutputStream(temp)) {out.write(bytes);out.getFD().sync();}
            read(temp);
            if(!temp.renameTo(target))throw new IOException("cannot stage helper");
            return target;
        } finally {if(temp.exists())temp.delete();}
    }
}
