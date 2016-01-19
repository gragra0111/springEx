package springbook.learningtest.template;

import java.io.BufferedReader;
import java.io.IOException;

public interface BufferedReaderCallback {
	Integer doSometingWithReader(BufferedReader br) throws IOException;
}
