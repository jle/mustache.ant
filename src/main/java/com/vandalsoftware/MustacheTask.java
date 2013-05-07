/*
 * Copyright 2013 Jonathan Le
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vandalsoftware;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * @author Jonathan Le
 */
public class MustacheTask extends Task {
    private String templatePath;
    private String outputPath;
    private String ymlPath;

    @Override
    public void execute() throws BuildException {
        BufferedReader ymlFileReader;
        try {
            ymlFileReader = new BufferedReader(new FileReader(this.ymlPath));
        } catch (FileNotFoundException e) {
            BuildException be = new BuildException("Unable to load " + this.ymlPath);
            be.initCause(e);
            throw be;
        }
        Yaml yaml = new Yaml();
        Object obj = yaml.load(ymlFileReader);
        closeSilently(ymlFileReader);

        if (this.outputPath != null) {
            // Write to a temporary location. This is to support the case where the outputPath
            // might be the same as the templatePath.
            BufferedWriter fileWriter = null;
            try {
                CharArrayWriter tempWriter = new CharArrayWriter();
                compile(obj, tempWriter);
                fileWriter = new BufferedWriter(new FileWriter(this.outputPath));
                tempWriter.writeTo(fileWriter);
            } catch (IOException e) {
                BuildException be = new BuildException("Couldn't open " + this.outputPath);
                be.initCause(e);
                throw be;
            } finally {
                flushSilently(fileWriter);
                closeSilently(fileWriter);
            }
            log("Wrote to " + this.outputPath);
        } else {
            compile(obj, new BufferedWriter(new OutputStreamWriter(System.out)));
        }
    }

    private void compile(Object obj, Writer writer) throws BuildException {
        Reader reader = null;
        try {
            try {
                reader = new BufferedReader(new FileReader(this.templatePath));
            } catch (FileNotFoundException e) {
                BuildException be = new BuildException("Couldn't open " + this.templatePath);
                be.initCause(e);
                throw be;
            }
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(reader, "ant");
            mustache.execute(writer, obj);
        } finally {
            closeSilently(reader);
            flushSilently(writer);
        }
    }

    public void setYml(String ymlFile) {
        this.ymlPath = ymlFile;
    }

    public void setFile(String filePath) {
        this.templatePath = filePath;
    }

    public void setToFile(String filePath) {
        this.outputPath = filePath;
    }

    private void flushSilently(Flushable f) {
        try {
            f.flush();
        } catch (IOException ignored) {
        }
    }

    private void closeSilently(Closeable c) {
        try {
            c.close();
        } catch (IOException ignored) {
        }
    }
}
