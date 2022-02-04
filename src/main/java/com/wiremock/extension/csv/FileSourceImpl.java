package com.wiremock.extension.csv;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;

import java.net.URI;
import java.util.List;

public class FileSourceImpl implements FileSource {
    @Override
    public BinaryFile getBinaryFileNamed(String s) {
        return null;
    }

    @Override
    public TextFile getTextFileNamed(String s) {
        return null;
    }

    @Override
    public void createIfNecessary() {

    }

    @Override
    public FileSource child(String s) {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public List<TextFile> listFilesRecursively() {
        return null;
    }

    @Override
    public void writeTextFile(String s, String s1) {

    }

    @Override
    public void writeBinaryFile(String s, byte[] bytes) {

    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public void deleteFile(String s) {

    }
}
