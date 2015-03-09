/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shadialian.linguistics.storage;

import edu.uci.ics.crawler4j.crawler.Page;
import java.util.Properties;

/**
 *
 * @author shadi
 */
public class FileStorage implements Storage{
    
    private String path ;
    
    private static final String documentsFile = "documents" ;
    
    private static final StringBuffer documentsBuffer = new StringBuffer();
    
    

    public FileStorage( Properties settings ){
        path = (String)settings.get("fileStoragePath");
    }
    @Override
    public void store(Page page) {
        
    }
    
}
