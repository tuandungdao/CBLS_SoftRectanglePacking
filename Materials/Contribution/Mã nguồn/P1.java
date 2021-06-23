package finalresult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class P1 {
	private String fileName;

	// Problem attributes
	private int N; // Number of soft rectangles
	private int L1; // Length
	private int L2; // Height
	private static float bestObjective; // The best Objective so far

	private int[] a; // Areas of the soft rectangles
	private float[] w; // Height of zone
	private float[] p; // Perimeter of plots

	/*
	 * Constructor without any parameters
	 */
	public P1(String _fileName) {
		fileName = _fileName;
	}

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
		int[] mem = new int[N];
		
		for (int i = 0; i < N; i++) {
			if(i < (N-3)) {
				int tmp = 1 + rn.nextInt(i+1);
				mem[tmp] = 1;
				x[i] = tmp;
			}
			else {
				int tmp = 1 + rn.nextInt((N-3));
				mem[tmp] = 1;
				x[i] = tmp;
				
			}
			//System.out.println();
			//System.out.print(" " + x[i]);
		}
		
		// index of zones 
		for(int i = 1; i < (N-3); i++) {
			for(int j = i + 1; j <= (N-3); j++) {
				if(mem[i] < mem[j]) {
					for(int k = 0; k < N; k++) {
						if(x[k]  == j) {
							x[k] = i;
						}
					}
					mem[i] = 1; mem[j] = 0;
				}
			}
		}
	}

	/*
	 * Printing a solution
	 */
	public void printSolution(int[] x, float obj){
		for(int i = 0; i < N; i++){
			System.out.print(" " + x[i]);
			
		}
		
		System.out.print(" " + obj);
	}
	
	/*
	 * Compute value of a solution
	 */
	public float computeSolValue(int[] x){
		float solValue = 0;
		
		for(int i = 0; i < N; i++) {
			Vector<Integer> vector= new Vector<>(); 
			int sum = 0, amax = 0;
			float lmax = 0;
			for(int j = 0; j < N; j++) {
				if(x[j] == i+1) {
					vector.add(j+1);
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
		return solValue * 2;
	}
	

	/*
	 * TabuSearch and Constraint-based local Search
	 */
	public void TabuConstraintBasedLocalSearch() {
		// Algorithm parameters
		int maxIt = 10000; // Maximum of iterations
		int stable = 0;
		int stableLimit = 10;

		// A solution
		int[] S = new int[N];
		float obj = 0; // Objective of Solution

		generateRandomlySolution(S); // Generate randomly a solution
		obj = computeSolValue(S);
		
		int[] bestSol = new int[N];
		bestSol = Arrays.copyOfRange(S, 0, S.length);
		float bestObj = obj; 
		
		//System.out.println("Randomly generated solution: ");
		//printSolution(S, obj);
		//System.out.println();
		//System.out.println("=========================================");
				
		Queue<Integer> tabu = new LinkedList<>(); // Tabu List
		int maxTabuSize = 5;
		int t = 0, t1 = 0, t2 = 0;
		
		int[] S1 =  new int[N];
		int[] S2 =  new int[N];
		int[] S3 =  new int[N];
		int[] S4 =  new int[N];

		float bestObjs = 1000;
		
		int it = 1;
		while(it < maxIt) {
			int[] tmp = new int [N];
			float n1 = L1 * L2;
			float n2 = L1 * L2;
			float n3 = L1 * L2;
			float n4 = L1 * L2;
			
			// N1(S) Change-based Neighborhood 
			for(int i = 0; i < N; i++) {
				int a = S[i];
				for(int j = 0; j < N; j++) {
					if((a != S[j]) && (tmp[S[j]] == 0) && !tabu.contains(i)) {
						tmp[S[j]] = 1;
						S[i] = S[j];
						float obj1 = computeSolValue(S);
						//printSolution(S, obj1);
						//System.out.println();
						if( obj1 < n1) {
							n1 = obj1;
							t = i;
							S1 =  Arrays.copyOfRange(S, 0, S.length);
						}
					}
					if((j == N-1) && !tabu.contains(i)) {
						int b = S[i];
						S[i] = (N-3) + 1;
						float obj1 = computeSolValue(S);
						//printSolution(S, obj1);
						//System.out.println();
						if (obj1 < n1) {
							n1 = obj1;
							t = i;
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
			
			if(n1 < obj) {
				tabu.add(t);
				if(tabu.size() > maxTabuSize) {
					tabu.remove();
				}
				stable = 0;
				obj = n1;
				S =  Arrays.copyOfRange(S1, 0, S1.length);
				//printSolution(S, obj);
				//System.out.print(tabu);
				//System.out.println();

			}
			else {
				// N2(S) Swap-based Neighborhood 
				for(int i = 0; i < N-1; i++) {
					int a = S[i];
					for(int j = i+1; j < N; j++) {
						if(a != S[j] && !tabu.contains(i) && !tabu.contains(j)) {
							int b = S[j];
							S[i] = S[j];
							S[j] = a;
							float val2 = computeSolValue(S);
							//printSolution(S, val2);
							//System.out.println("");
							if( val2 < n2) {
								t1 = i;
								t2 = j;
								n2 = val2 ;
								S2 =  Arrays.copyOfRange(S, 0, S.length);
							}
							S[j] = b;
							S[i] = a;
						}		
					}
					S[i] = a;	
				}
				
				
				if(n2 < obj) {
					tabu.add(t1);
					tabu.add(t2);
					while(tabu.size() > maxTabuSize) {
						tabu.remove();
					}
					stable = 0;
					S =  Arrays.copyOfRange(S2, 0, S2.length);
					obj = n2;
					//printSolution(S, obj);
					//System.out.print(tabu);
				} 
				else {
					int zmax = 0, z = 0, zmin = 50, m = 0;

					for (int i = 1; i <= (N-3); i++) {
						int count = 0;//
						for (int j = 0; j < N; j++) {
							if (S[j] == i) {
								count++;
							}

						}
						if (count > zmax) {
							zmax = count;
							z = i;
						}
						if (count < zmin && count != 0) {
							zmin = count;
							m = i;
						}
					}
					int tb = zmax / 2;
					// System.out.println(z);
					// System.out.println(zmax);
					//System.out.println(m);
					//System.out.println(zmin);
					
					// N3(S) Split-based Neighborhood 
					for (int i = 0; i < N; i++) {
						if (S[i] == z && (tb != 0)) {
							S[i] = (N-3) + 2;
							tb--;
						}
					}
					float obj3 = computeSolValue(S);
					S3 = Arrays.copyOfRange(S, 0, S.length);
					
					for (int i = 0; i < N; i++) {
						if (S[i] == ((N-3) + 2)) {
							S[i] = z;
						}
					}
					if (obj3 < obj) {
						stable = 0;
						obj = obj3;
						S = Arrays.copyOfRange(S3, 0, S3.length);
						//printSolution(S, obj);
						//System.out.print("#");
						//System.out.println();
					}
					else {
						int[] q = new int[N];
						int[] r = new int[N];

						for (int k = 0; k < N; k++) {
							if (S[k] == m) {
								r[k] = 1;
							}
						}
						
						// N4(S) Merge-based Neighborhood 
						for (int i = 0; i < N; i++) {
							if (S[i] != m && (q[S[i]] == 0)) {
								q[S[i]] = 1;
								for (int j = 0; j < N; j++) {
									if (S[j] == m) {
										S[j] = S[i];
									}
								}
								float obj4 = computeSolValue(S);
								// S4 = Arrays.copyOfRange(S, 0, S.length);
								//printSolution(S, obj4);
								//System.out.println();
								if (obj4 < n4) {
									n4 = obj4;
									S4 = Arrays.copyOfRange(S, 0, S.length);
								}
							}
							for (int k = 0; k < N; k++) {
								if (r[k] == 1) {
									S[k] = m;
								}
							}
						}
						q = new int[N];
						if(n4 < obj) {
							stable = 0;
							obj = n4;
							S = Arrays.copyOfRange(S4, 0, S4.length);
							//printSolution(S, obj);
							//System.out.print("##");
							//System.out.println();
						}
						else {
							stable ++;
							S =  Arrays.copyOfRange(S2, 0, S2.length);
							obj = computeSolValue(S);
							//printSolution(S, obj);
							//System.out.print("###");
							//System.out.println();
						}
					}
					
				}
				
			}
			
			//System.out.print("stable" + stable);
			
			if(obj < bestObj) {
				bestSol =  Arrays.copyOfRange(S, 0, S.length);
				bestObj = obj;
				//printSolution(bestSol, bestObj);
				//System.out.println();
			}
			
			if(stable > stableLimit) {
				if(bestObj < bestObjs) {
					bestObjs = bestObj;
				}
				generateRandomlySolution(S);
				obj = computeSolValue(S);
				//System.out.println("Randomly generated solution: ");
				//printSolution(S, obj);
				//System.out.println();
				tabu.clear();
				bestSol = Arrays.copyOfRange(S, 0, S.length);
				bestObj = obj; 
			}
			
			if(bestObjs > 100 && (it % 1000) == 0) {
				bestObjs = bestObj;
			}
			
			it++;
			//printSolution(bestSol, bestObj);	
		}
		
		bestObjective = bestObjs;
		//System.out.println(bestObjective+" " + bestObj + " "+ bestObjs);
		//System.out.print("Best Solution: ");
		//printSolution(bestSol, bestObj);
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		long totalTime = 0;
		float sumOfObj = 0;
		float maxObj = 0;
		float minObj = 1000;
		final long[] time = new long[11];
		time[0] = System.currentTimeMillis();
		
		for(int i = 1; i <=  10; i++) {
			P1 softrectangle = new P1("D:/CBLS_Base/Instances_Soft_Packing/Class MN/p21.txt");
			//P1 softrectangle = new P1("D:/CBLS_Base/Instances_Soft_Packing/Class MU/p21.txt");
			//P1 softrectangle = new P1("D:/CBLS_Base/Instances_Soft_Packing/Class U/p21.txt");
			softrectangle.readProblemInstanceFromFile();
			softrectangle.TabuConstraintBasedLocalSearch();
		
			time[i] = System.currentTimeMillis();
			long t = time[i] - time[i-1];
			totalTime += t;
			sumOfObj += bestObjective;
			if(bestObjective > maxObj) {
				maxObj = bestObjective;
			}
			if(bestObjective < minObj) {
				minObj = bestObjective;
			}
			
			//System.out.println();
			//System.out.println("Total execution time: " + t * 0.001);
			if(i == 10) {
				System.out.println(totalTime * 0.0001 + " " + sumOfObj/10.0 + " " + maxObj + " " + minObj);
				//System.out.println("Max of Objective = " + maxObj + " Min of Objective = " + minObj);
			}
			
			/*
			 * Write the output to a file .txt
			 */ 
			try {
				File file = new File("D:/CBLS_Base/testout.txt");
        	 
				if(!file.exists()){
					file.createNewFile();
				}
        	 
				FileWriter fw = new FileWriter(file,true);
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write("Best Objective: " + bestObjective +" Time: " + t * 0.001 + "\n");
				if(i == 10) {
					bw.write("Average of Time: " + totalTime * 0.0001 + " Average of Best Ojective: " + sumOfObj/10.0);
					bw.write(" Max Objective: " + maxObj + " Min Objective: " + minObj);
				}
				
				bw.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		
		}
		
		/*
		 * Write the output to a file Excel
		 *
		try {
			FileInputStream fileEx = new FileInputStream("D:/CBLS_Base/test1.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(fileEx);
			Sheet sheet = wb.getSheetAt(0); 
			    int num = sheet.getLastRowNum(); 
			    Row row = sheet.createRow(++num); 
			    row.createCell(0).setCellValue(totalTime * 0.0001);
			    row.createCell(1).setCellValue(sumOfObj/10.0); 
			    row.createCell(2).setCellValue(maxObj);
			    row.createCell(3).setCellValue(minObj);
			    
			    // Write the output to a file 
			    FileOutputStream fileOut = new FileOutputStream("D:/CBLS_Base/test1.xlsx"); 
			    wb.write(fileOut); 
			fileOut.close(); 
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
		}  catch (IOException e) {
	        e.printStackTrace();
	    }*/
		
	}

}
