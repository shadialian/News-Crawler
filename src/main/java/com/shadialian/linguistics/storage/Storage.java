/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shadialian.linguistics.storage;

import edu.uci.ics.crawler4j.crawler.Page;

/**
 *
 * @author shadi
 */
public interface Storage {
    
    public void store( Page page );
    
}
