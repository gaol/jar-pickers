/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.eap.trackers.ircbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author lgao
 *
 */
public class AnswerMeLoader {
    
    private static final HashMap<Class<? extends AnswerMe>, Set<String>> answers = new HashMap<Class<? extends AnswerMe>, Set<String>>();
    
    public static final AnswerMeLoader INSTANCE = new AnswerMeLoader();
    private static final String PROP_FILE = "answermes.properties";
    private static final String SEP = ",";
    
    private AnswerMeLoader() {
        // private constructor
        loadAnswers();
    }

    @SuppressWarnings("unchecked")
    private void loadAnswers() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(PROP_FILE);
        if (in == null) {
            throw new IllegalStateException("No answers properties file found.");
        }
        Properties prop = new Properties();
        try {
            prop.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Can't load answers properties file.", e);
        }
        for (Map.Entry<Object, Object> entry: prop.entrySet()) {
            String clsName = entry.getKey().toString();
            String patterns = entry.getValue().toString();
            
            // load class
            Class<? extends AnswerMe> answerMe = null;
            try {
                answerMe = (Class<? extends AnswerMe>) Class.forName(clsName);
                
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Can't load AnswerMe Class: " + clsName, e);
            }
            Set<String> patternSet = new HashSet<String>();
            String[] patternArray = patterns.split(SEP);
            for (String pattern : patternArray) {
                patternSet.add(pattern.trim());
            }
            answers.put(answerMe, patternSet);
        }
    }
    
    
    public AnswerMe getAnswerMe(String pattern) {
        for (Map.Entry<Class<? extends AnswerMe>, Set<String>> entry: answers.entrySet()) {
            if (entry.getValue().contains(pattern)) {
                try {
                    AnswerMe answerMe = entry.getKey().newInstance();
                    answerMe.setPattern(Pattern.compile(pattern));
                    return answerMe;
                } catch (Exception e) {
                    throw new RuntimeException("Can't instance class: " + entry.getKey().getName(), e);
                } 
            }
        }
        return null;
    }
    
    public AnswerMe getAnswerMeBySentence(String sentence) {
        for (Map.Entry<Class<? extends AnswerMe>, Set<String>> entry: answers.entrySet()) {
            for (String pattern: entry.getValue()) {
                Pattern p = Pattern.compile(pattern);
                if (p.matcher(sentence).matches()) {
                    try {
                        AnswerMe answerMe = entry.getKey().newInstance();
                        answerMe.setPattern(p);
                        return answerMe;
                    } catch (Exception e) {
                        throw new RuntimeException("Can't instance class: " + entry.getKey().getName(), e);
                    } 
                }
            }
        }
        return null;
    }
    

}
