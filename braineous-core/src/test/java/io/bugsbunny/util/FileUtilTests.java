package io.bugsbunny.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import com.google.common.collect.ImmutableList;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtilTests
{
    private static Logger logger = LoggerFactory.getLogger(FileUtilTests.class);

    @Test
    public void testGetStarted() throws Exception
    {
        // For a simple file system with Unix-style paths and behavior:
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Path foo = fs.getPath("/foo");
        Files.createDirectory(foo);

        Path hello = foo.resolve("hello.txt"); // /foo/hello.txt
        Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);

        //Read the file
        String content = Files.readString(hello);
        logger.info(content);
    }
}
