/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shadialian.linguistics.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections4.trie.PatriciaTrie;
/**
 *
 * @author shadi
 */
public class Data implements Serializable {
    
    private final long serialVersionUID = 123123123123123l;
    private static final transient Logger logger = LoggerFactory.getLogger(Data.class);    
    private static final transient Logger sections = LoggerFactory.getLogger("com.shadialian.sections");
    private static final transient Logger tags = LoggerFactory.getLogger("com.shadialian.tags");
    private static final transient Logger sources = LoggerFactory.getLogger("com.shadialian.sources");

    private final PatriciaTrie<Integer> sectionsMap = new PatriciaTrie<>();
    private final PatriciaTrie<Integer> tagsMap = new PatriciaTrie<>();
    private final PatriciaTrie<Integer> sourceMap = new PatriciaTrie<>();
    private long fileCounter = 0;

    private Data(){}
    
    private static Data data ;
    private static final String path = "local-data";
    
    public static Data getData(){
        if( data == null )
            data = readObject();
        if( data == null )
            data = readFromFiles();
        if( data == null )
            data = new Data();
        return data ;
    }
    
    public void setCounter( int count ){
        fileCounter = count;
    }
    
    private static Data readFromFiles(){
        Data temp = new Data();
        try {
            
            Scanner secScan = new Scanner(new File("logs/sections.log")).useDelimiter(";|\\n");
            while( secScan.hasNext() ){   
                int id = secScan.nextInt();
                String string = secScan.next();
                temp.sectionsMap.put(string,id);
            }
            secScan.close();
            Scanner tagScan = new Scanner(new File("logs/tags.log")).useDelimiter(";|\\n");
            while( tagScan.hasNext() ){  
                int id = tagScan.nextInt();
                String string = tagScan.next();
                temp.tagsMap.put(string,id);
            }
            tagScan.close();
            Scanner sorScan = new Scanner(new File("logs/sources.log")).useDelimiter(";|\\n");
            while( sorScan.hasNext() ){   
                int id = sorScan.nextInt();
                String string = sorScan.next();
                temp.sourceMap.put(string,id);
            }
            sorScan.close();
            return temp ;
        } catch (FileNotFoundException ex) {
            logger.warn("Could not find ./logs/sections.log");
            return null;
        }
    }
    
    /**
     *
     * @throws Throwable
     */
    @Override
    public void finalize() throws Throwable{        
        writeObject();
        super.finalize();
    }
    
    private static Data readObject(){
        try {
            FileInputStream fis = new FileInputStream(path + "/data.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (Data) ois.readObject();
        }catch( IOException | ClassNotFoundException e ){
            logger.warn("No serialised object");
            return null;
        }
    }
    
    private static void writeObject(){
        new File(path + "/data.ser").delete();
        try{ 
        FileOutputStream fos = new FileOutputStream(path + "/data.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
        }catch( Exception e ){
            logger.warn("Could not write serialized object");
        }
    }
    public synchronized long writeFile(String content) {
        try {
            PrintWriter file = new PrintWriter("files/" + fileCounter++ + ".txt");
            file.print(content);
            file.close();
            return fileCounter;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public synchronized int addSectionIfNotExistAndReturnId(String section) {
        if (sectionsMap.containsKey(section)) {
            return sectionsMap.get(section);
        }
        int id = sectionsMap.size();
        sectionsMap.put(section, id);
        sections.info("{};{}", id, section);
        return id;
    }

    public synchronized int addSourceIfNotExistAndReturnId(String source) {
        if (sourceMap.containsKey(source)) {
            return sourceMap.get(source);
        }
        int id = sourceMap.size();
        sourceMap.put(source, id);
        sources.info("{};{}", id, source);
        return id;
    }

    public synchronized int addTagIfNotExistAndReturnId(String tag) {
        if (tagsMap.containsKey(tag)) {
            return tagsMap.get(tag);
        }
        int id = tagsMap.size();
        tagsMap.put(tag, id);
        tags.info("{};{}", id, tag);
        return id;
    }
}
