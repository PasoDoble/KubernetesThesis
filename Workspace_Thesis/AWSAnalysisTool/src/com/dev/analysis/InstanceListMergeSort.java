package com.dev.analysis;

public class InstanceListMergeSort {

	private InstanceTypeDefinition[] sortingArray;
	private InstanceTypeDefinition[] merger;
	private int length;
	private int sortFactor; //0 = availability, 1 = average price, 2 = simple hybrid sorting, 3 = BaselineFactor

	public InstanceListMergeSort(){}

	public InstanceTypeDefinition[] sort(InstanceTypeDefinition[] in, int sf){
		sortFactor = sf;
		sortingArray = in;
		length = in.length;
		merger = new InstanceTypeDefinition[length];

		mergeSort(0, length-1);
		return sortingArray;
	}

	public void mergeSort(int lower, int upper){
		if(lower<upper){
			int mid = lower + ((upper-lower)/2);

			mergeSort(lower, mid);

			mergeSort(mid+1, upper);

			switch (sortFactor){
			case 0: mergePartsAvail(lower, mid, upper);
					break;
			case 1: mergePartsPPB(lower, mid, upper);
					break;
			case 2: mergePartsSimpleHybrid(lower, mid, upper);
					break;
			case 3: mergePartsByBaselineFactor(lower, mid, upper);
					break;
			}
		}
	}

	public void mergePartsAvail(int lower, int mid, int upper){
		for (int i = lower; i <= upper; i++) {
			merger[i] = sortingArray[i];
		}
		int i = lower;
		int j = mid;
		int k = lower;
		while(i <= mid && j <= upper){
			if(merger[i].getAvailability() > merger[j].getAvailability()){
				sortingArray[k] = merger[i];
				i++;
			}
			else if(merger[i].getAvailability() == merger[j].getAvailability()){
				if(merger[i].getAverageCost() <= merger[j].getAverageCost()){
					sortingArray[k] = merger[i];
					i++;
				}
				else{
					sortingArray[k] = merger[j];
					j++;
				}
			}
			else{
				sortingArray[k] = merger[j];
				j++;
			}
			k++;
		}
		while(i <= mid){
			//System.out.println("k = "+k+"i = "+i);
			sortingArray[k] = merger[i];
			k++;
			i++;
		}
	}

	public void mergePartsPPB(int lower, int mid, int upper){
		for (int i = lower; i <= upper; i++) {
			merger[i] = sortingArray[i];
		}
		int i = lower;
		int j = mid;
		int k = lower;
		while(i <= mid && j <= upper){
			if(merger[i].getAverageCost() < merger[j].getAverageCost()){
				sortingArray[k] = merger[i];
				i++;
			}
			else if(merger[i].getAverageCost() == merger[j].getAverageCost()){
				if(merger[i].getAvailability() <= merger[j].getAvailability()){
					sortingArray[k] = merger[i];
					i++;
				}
				else{
					sortingArray[k] = merger[j];
					j++;
				}
			}
			else{
				sortingArray[k] = merger[j];
				j++;
			}
			k++;
		}
		while(i <= mid){
			//System.out.println("k = "+k+"i = "+i);
			sortingArray[k] = merger[i];
			k++;
			i++;
		}
	}

	//Hybrid factor: cost/availability, the lowest values are theoretically optimal
	public void mergePartsSimpleHybrid(int lower, int mid, int upper){
		for (int l = lower; l <= upper; l++) {
			merger[l] = sortingArray[l];
		}
		int i = lower;
		int j = mid+1;
		int k = lower;
		double iHybridFactor;
		double jHybridFactor;
		while(i <= mid && j <= upper){
			//System.out.println("k = "+k+"   i = "+i+"   j = "+j);
			iHybridFactor = merger[i].getAverageCost()/merger[i].getAvailability();
			jHybridFactor = merger[j].getAverageCost()/merger[j].getAvailability();
			if(iHybridFactor < jHybridFactor){
				sortingArray[k] = merger[i];
				i++;
			}
			else{
				sortingArray[k] = merger[j];
				j++;
			}
			k++;
		}
		while(i <= mid){
			//System.out.println("k = "+k+"   i = "+i);
			sortingArray[k] = merger[i];
			k++;
			i++;
		}
	}
	
	public void mergePartsByBaselineFactor(int lower, int mid, int upper){
		for (int l = lower; l <= upper; l++) {
			merger[l] = sortingArray[l];
		}
		int i = lower;
		int j = mid+1;
		int k = lower;
		while(i <= mid && j <= upper){
			//System.out.println("k = "+k+"   i = "+i+"   j = "+j);
			if(merger[i].getBaselineFactor() > merger[j].getBaselineFactor()){
				sortingArray[k] = merger[i];
				i++;
			}
			else{
				sortingArray[k] = merger[j];
				j++;
			}
			k++;
		}
		while(i <= mid){
			//System.out.println("k = "+k+"   i = "+i);
			sortingArray[k] = merger[i];
			k++;
			i++;
		}
	}


}
