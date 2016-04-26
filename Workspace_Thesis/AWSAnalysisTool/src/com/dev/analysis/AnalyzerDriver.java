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
        
        SpotHistoryAnalyzer spotHistory = new SpotHistoryAnalyzer(credentials);
        /*try {
			spotHistory.analyzeHistory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        /*try {
			spotHistory.separateRawData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        //spotHistory.createFileDictionary();
        
        /*try {
			spotHistory.calculatePricePerBaseline();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        /*try {
			spotHistory.tallyingTheAbsoluteCosts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        /*try {
			spotHistory.tallyingTheBaselinedCosts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        /*try {
			spotHistory.createAvailabilityList(.01);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        try {
        	InstanceTypeDefinition[] listOfThings = spotHistory.listBySimpleHybrid(.02);
        	spotHistory.obtainInstances(listOfThings);
        	//for(InstanceTypeDefinition yes : listOfThings){
            //	System.out.println("Driver Working For: "+ yes.getLabel());
            //}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        
	}
}
