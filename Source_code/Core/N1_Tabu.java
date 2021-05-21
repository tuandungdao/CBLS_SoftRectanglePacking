package core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

public class N1_Tabu {
	private String fileName;

	// Problem attributes
	private int N; // Number of soft rectangles
	private int L1; // Length
	private int L2; // Height

	private int[] a; // Areas of the soft rectangles
	private float[] w; // Height of zone
	private float[] l; // Length of plots
	private float[] p; // Perimeter of plots

	/*
	 * Constructor without any parameters
	 */
	public N1_Tabu(String _fileName) {
		fileName = _fileName;
	}

	// Input
	public void readProblemInstanceFromFile() {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String line;
			
			// Line 1: #N L1 L2
			if ((line = br.readLine()) != null){
				String[] elements = line.split("\\s");
				int x[] = new int[3];
				for(int j = 0; j < 3; j++){
					x[j] = Integer.parseInt(elements[j]);
					//System.out.println(s[j]);
				}
				N = x[0]; L1 = x[1]; L2 = x[2];
			}
			
			// #a[i]
			int i = 1;
			a = new int[N+1];			
			while((line = br.readLine()) != null && i <= N) {
				a[i] = Integer.parseInt(line);
				//System.out.println("a[" + i + "] = " + a[i]);
				i++;
			}
			
			// Close the input stream
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/*
	 * Generate randomly a solution
	 */
	void generateRandomlySolution(int[] x) {
		Random rn = new Random();
		
		for (int i = 0; i < N; i++) {
			int tmp = 1 + rn.nextInt(N/3);
			x[i] = tmp;
			//System.out.println("a[" + (i+1) + "] in zone "+ tmp);
		}

	}

	/*
	 * Printing a solution
	 */
	public void printSolution(int[] x, float val){
		for(int i = 0; i < N; i++){
			System.out.print(" " + x[i]);
			
		}
		
		System.out.print(" " + val);
	}
	
	/*
	 * Compute value of a solution
	 */
	public float computeSolValue(int[] x){
		float solValue = 0;
		for(int i = 0; i < N; i++) {
			Vector<Integer> vector= new Vector<>(); 
			int sum = 0, count = 0, amax = 0;
			float lmax = 0;
			for(int j = 0; j < N; j++) {
				if(x[j] == i+1) {
					vector.add(j+1);
					count++;
					sum += a[j+1];
					if(a[j+1] > amax) amax = a[j+1]; 
				}
			}
			//System.out.println(vector);
			w = new float[N+1];
			p = new float[N+1];
			if(sum != 0) {
				w[i] = (float)sum / L1;
				lmax = amax / w[i];
				p[i] = w[i] + lmax;
				if(p[i] > solValue) solValue = p[i];
			}
			else { 
				w[i] = p[i] = 0;
			}
			
			 //System.out.println(w[i]);
			 //System.out.println(p[i]);
		}
		//System.out.println(solValue);
		return solValue;
	}
	

	/*
	 * TabuSearch and Constraint-based local Search
	 */
	public void TabuConstraintBasedLocalSearch() {
		// Algorithm parameters
		int maxIt = 500; // Maximum of iterations
		int stable = 0;
		int stableLimit = 10;
		int restartFreq = 50;
		
		// A solution
		int[] S = new int[N];
		float obj = 0; // Objective of Solution

		generateRandomlySolution(S); // Generate randomly a solution
		obj = computeSolValue(S);
		
		int[] bestSol = new int[N];
		bestSol = Arrays.copyOfRange(S, 0, S.length);
		float bestObj = obj;
		float obj1;
		
		System.out.println("Randomly generated solution: ");
		printSolution(S, obj);

		int[] S1 =  new int[N];  		
		Queue<Integer> tabu = new LinkedList<>();
		int maxTabuSize = 3;
		int t1 = 0;
		
		Random rn = new Random();
		
		int it = 1;
		while(it < maxIt) {
			float v = L1 * L2;	
			int t = 0;
			System.out.println();
			System.out.println("=========================================");
			int[] tmp = new int [N];


			for(int i = 0; i < N; i++) {
				int a = S[i];
				for(int j = 0; j < N; j++) {
					if(a != S[j] && tmp[S[j]] == 0 && !tabu.contains(i)) {
						tmp[S[j]] = 1;
						S[i] = S[j];
						obj1 = computeSolValue(S);
						//printSolution(S, obj1);
						//System.out.println();
						if( obj1 < v) {
							v = obj1;
							t = i;
							//printSolution(S1, v);
							S1 =  Arrays.copyOfRange(S, 0, S.length);
						}
					}
					if(j == N-1 && !tabu.contains(i)) {
						int b = S[i];
						S[i] = N/3 + 1;
						obj1 = computeSolValue(S);
						//printSolution(S, obj1);
						//System.out.println();
						if (obj1 < v) {
							t = i;
							v = obj1;
							S1 =  Arrays.copyOfRange(S, 0, S.length);
						}
						else {
							S[i] = b;
						}
					}
				
				}
				tmp = new int [N];
				S[i] = a;
			}
			obj = v;
			S = Arrays.copyOfRange(S1, 0, S1.length);
			
			if(obj < bestObj) {
				t1 = t;
				bestObj = obj;
				bestSol = Arrays.copyOfRange(S, 0, S.length);
			}
			else {
				obj = computeSolValue(S);
				if(stable == stableLimit) {
					System.out.print("**");
					t = t1;
					S =  Arrays.copyOfRange(bestSol, 0, bestSol.length);
					obj = computeSolValue(S);
					stable = 0;
				}
				else { 
					t = rn.nextInt(N);
					while(tabu.contains(t)) {
						t = rn.nextInt(N);
					}
					int x = S[t];
					S[t] = 1 + rn.nextInt(N/3);
					while(S[t] == x) {
						S[t] = 1 + rn.nextInt(N/3);
					}
					stable++;
					//System.out.print("*");
				}
			}
			
			tabu.add(t);
			if(tabu.size() > maxTabuSize) {
				tabu.remove();
			}
			
			printSolution(S, obj);
			printSolution(bestSol, bestObj);
			System.out.print(" ");
			System.out.print(tabu);
			if(it % restartFreq == 0) {
				//System.out.println();
				//printSolution(bestSol, bestObj);
				generateRandomlySolution(S);
				obj = computeSolValue(S);
				System.out.println();
				System.out.println("Randomly generated solution: ");
				printSolution(S, obj);
				tabu.clear();
				bestSol = Arrays.copyOfRange(S, 0, S.length);
				bestObj = obj; 
			}
			//printSolution(bestSol, bestObj);
			it++;
		}
		//printSolution(bestSol, bestObj);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		N1_Tabu softrectangle = new N1_Tabu("D:/CBLS_Base/Instances_Soft_Packing/Class MN/p01.txt");
		//CBLS_Base softrectangle = new CBLS_Base("D:/CBLS_Base/Instances_Soft_Packing/Class MU/p01.txt");
		//CBLS_Base softrectangle = new CBLS_Base("D:/CBLS_Base/Instances_Soft_Packing/Class U/p01.txt");
		softrectangle.readProblemInstanceFromFile();
		softrectangle.TabuConstraintBasedLocalSearch();
	}

}
