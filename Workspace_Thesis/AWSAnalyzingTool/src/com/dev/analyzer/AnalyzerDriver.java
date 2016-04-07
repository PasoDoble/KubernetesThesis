package com.dev.analyzer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class AnalyzerDriver {
	public static void main(String[] args) {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (/home/justin/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/home/justin/.aws/credentials), and is in valid format.",
                    e);
        }
        
        SpotHistoryAnalyzer spotHistory = new SpotHistoryAnalyzer(credentials);
        spotHistory.analyzeHistory();
        
        
	}
}
