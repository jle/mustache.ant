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
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

        Writer writer;
        final boolean fileOutput = this.outputPath != null;
        if (fileOutput) {
            try {
                writer = new BufferedWriter(new FileWriter(this.outputPath));
            } catch (IOException e) {
                BuildException be = new BuildException("Couldn't open " + this.outputPath);
                be.initCause(e);
                throw be;
            }
        } else {
            writer = new OutputStreamWriter(System.out);
        }
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(this.templatePath);
        mustache.execute(writer, obj);
        try {
            writer.flush();
        } catch (IOException e) {
            BuildException be = new BuildException("Couldn't write to output");
            be.initCause(e);
            throw be;
        }
        closeSilently(writer);
        if (fileOutput) {
            log("Wrote to " + this.outputPath);
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

    private void closeSilently(Closeable c) {
        try {
            c.close();
        } catch (IOException ignored) {
        }
    }
}
