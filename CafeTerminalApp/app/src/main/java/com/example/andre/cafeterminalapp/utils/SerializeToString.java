package com.example.andre.cafeterminalapp.utils;

/**
 * Created by andre on 03/11/2016.
 */

import android.util.Base64;
import java.io.*;

/**
 * Usage sample serializing SomeClass instance
 */
class SerializeToString {


    /** Read the object from Base64 string. */
    static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}

