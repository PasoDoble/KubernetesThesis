package com.dev.analysis;

import java.io.IOException;

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
        
        SpotHistoryAnalyzer spotHistory = new SpotHistoryAnalyzer(credentials, .02);
        try {
			//spotHistory.analyzeHistory();
        	//spotHistory.separateRawData();
        	//spotHistory.calculatePricePerBaseline();
        	//spotHistory.tallyingTheAbsoluteCosts();
        	//spotHistory.tallyingTheBaselinedCosts();
        	//spotHistory.createAvailabilityList(.01);
        	//InstanceTypeDefinition[] listOfThings = spotHistory.listBySimpleHybrid();
        	//spotHistory.obtainInstances(listOfThings);
        	boolean[][] timeMap = spotHistory.createAllInstancesTimeMap();
        	double[][] interAvailabilityMap = spotHistory.determineInstanceIndependence(timeMap);
        	//spotHistory.createOrderedIDFileList();
        	//for(InstanceTypeDefinition yes : listOfThings){
            //	System.out.println("Driver Working For: "+ yes.getLabel());
            //}
        	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        //spotHistory.createFileDictionary();
        

	}
}
