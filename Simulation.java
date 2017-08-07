import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Scanner;


public class Simulation {

	/**
	 * @param args
	 */
	String instruction,op1,op2,op3;
	
	static BufferedReader reader = null;
	
	public Simulation()
	{
		String instruction=null;
		String op1=null;
		String op2=null;
		String op3=null;
	}
	
	static Simulation[] stage=new Simulation[5];
	public static int[] registers=new int[8];
	public static int X;
	public static int[] memory=new int[10000];
	public static int PC=20000;
	public static int cycle_count;
	public static int[] valid=new int[8];	
	public static int stall=0;
	public static int offset;
	private static enum TokenKind
	{
		ADD,
		SUB,
		MUL,
		MOVC,
		AND,
		OR,
		XOR,
		LOAD,
		STORE,
		BZ, 
		BNZ, 
		JUMP, 
		BAL, 
		HALT
		
	}
	public static Simulation simulation=new Simulation();
	
	static void initialize()
	{
		simulation.PC=20000;
		cycle_count=0;
		X=0;
		for(int i=0;i<5;i++)
			stage[i]=new Simulation();
		for(int i=0;i<5;i++)
		{
			stage[i].instruction="";
			stage[i].op1="";
			stage[i].op2="";
			stage[i].op3="";
		}
		System.out.println("\nArchitectural registers:");
		for (int i=0;i<8;i++)
			System.out.println("R"+i+":"+registers[i]);
		for (int i=0;i<8;i++)
			simulation.valid[i]=1;
		System.out.println("Program counter:"+simulation.PC);
	}
	
	static void fetch(BufferedReader reader)
	{		
		if(stage[0].instruction!=null)
		{
		String nextline;
		try {
			nextline = reader.readLine();
			if(nextline!=null)
			{
				stage[0].instruction = nextline;
				System.out.println("Fetch stage: "+stage[0].instruction);
				System.out.println("\nPC:"+simulation.PC);
				simulation.PC++;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
			else
				return;	
	}
	
	static void decode(BufferedReader reader)
	{
		if(stage[1].instruction!=null)
		{	
		stage[1].instruction=stage[0].instruction;
		String line= stage[1].instruction;
		if(line!=null)
		{
			String[] token= line.split(",|\\s");
			if(token[0].matches("ADD|SUB|MUL|AND|OR|EX-OR"))
			{
				stage[1].instruction=token[0];
				stage[1].op1=token[1];
				stage[1].op2=token[2];
				stage[1].op3=token[3];
				int src1=Integer.valueOf(stage[1].op2.charAt(1))-'0';
				int src2=Integer.valueOf(stage[1].op3.charAt(1))-'0';
				if(valid[src1]==0 || valid[src2]==0)
					simulation.stall=1;
			}
			if(token[0].matches("LOAD|STORE"))
			{
				stage[1].instruction=token[0];
				stage[1].op1=token[1];
				stage[1].op2=token[2];
				stage[1].op3=token[3];
				int src1=Integer.valueOf(stage[1].op1.charAt(1))-'0';
				int src2=Integer.valueOf(stage[1].op2.charAt(1))-'0';
				if(valid[src1]==0 || valid[src2]==0)
					simulation.stall=1;
			}
			if(token[0].matches("MOVC|MOV|JUMP|BAL"))
			{
				stage[1].instruction=token[0];
				stage[1].op1=token[1];
				stage[1].op2=token[2];
				stage[1].op3="";
			}
			if(token[0].matches("BZ|BNZ"))
			{
				stage[1].instruction=token[0];
				stage[1].op1=token[1];
				stage[1].op2="";
				stage[1].op3="";
			}
			if(token[0].equals("HALT"))
			{
				stage[1].instruction=token[0];
				stage[1].op1="";
				stage[1].op2="";
				stage[1].op3="";
			}
			stage[0].instruction="";
			//stage[0].op1=null;
			//stage[0].op2=null;
			//stage[0].op3=null;
			System.out.println("Decode stage:\t"+stage[1].instruction+" "+stage[1].op1+" "+stage[1].op2+" "+stage[1].op3);
			fetch(reader);
		}
		else
			return;
		}
	}
	
	static void execute(String filename)
	{
	
		int dest = 0,src1 = 0,src2 = 0,literal;
		if(stage[1].op1!=null || stage[1].op2!=null)
		{
			if(stage[1].op1.startsWith("R"))
			dest=Integer.valueOf(stage[1].op1.charAt(1))-'0';
			if(stage[1].op2.startsWith("R"))
			src1=Integer.valueOf(stage[1].op2.charAt(1))-'0';
			if(stage[1].op3!=null)
			{
				if(stage[1].op3.startsWith("R"))
					src2=Integer.valueOf(stage[1].op3.charAt(1))-'0';
			}
			if(simulation.stall==1)
			{
				if(valid[dest]==0 || valid[src1]==0 || valid[src2]==0)
					return;
				else
					simulation.stall=0;
			}
				stage[2].instruction=stage[1].instruction;
				stage[2].op1=stage[1].op1;
				stage[2].op2=stage[1].op2;
				stage[2].op3=stage[1].op3;
		
				if(stage[2].instruction!=null)
					{
						if(stage[2].instruction.matches("ADD|SUB|MUL|AND|OR|EX-OR"))
						{
							src1=Integer.valueOf(stage[2].op2.charAt(1))-'0';
							src2=Integer.valueOf(stage[2].op3.charAt(1))-'0';
				
							if(stage[2].instruction.equals("ADD"))
								simulation.registers[dest]=simulation.registers[src1]+Simulation.registers[src2];
							if(stage[2].instruction.equals("MUL"))
								simulation.registers[dest]=simulation.registers[src1]*simulation.registers[src2];
							if(stage[2].instruction.equals("SUB"))
								simulation.registers[dest]=(simulation.registers[src1])-(simulation.registers[src2]);
							if(stage[2].instruction.equals("AND"))
								simulation.registers[dest]=simulation.registers[src1]&Simulation.registers[src2];
							if(stage[2].instruction.equals("OR"))
								simulation.registers[dest]=simulation.registers[src1]|Simulation.registers[src2];
							if(stage[2].instruction.equals("EX-OR"))
								simulation.registers[dest]=simulation.registers[src1]^Simulation.registers[src2];
						}
						if(stage[2].instruction.equals("MOVC"))
						{
				//dest=Integer.valueOf(stage[2].op1.charAt(1))-'0';
							literal=Integer.valueOf(stage[2].op2);
							simulation.registers[dest]=literal;
						}
						if(stage[2].instruction.matches("JUMP"))
						{
							literal=Integer.valueOf(stage[2].op2);
							if(stage[2].op1.matches("X"))
								simulation.PC=simulation.X+literal;
							else
								simulation.PC=simulation.registers[dest]+literal;
							simulation.offset=simulation.PC-20000;
							stage[1].instruction="";
							stage[1].op1="";
							stage[1].op2="";
							stage[1].op3="";
							stage[0].instruction="";
							stage[0].op1="";
							stage[0].op2="";
							stage[0].op3="";
							
							try {
								reader.close();
								reader=new BufferedReader(new FileReader(filename));
							
							for(int j=0;j<simulation.offset;j++)
								reader.readLine();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return;
						}
						if(stage[2].instruction.equals("BAL"))
						{
							literal=Integer.valueOf(stage[2].op2);
							simulation.X=simulation.PC+1;
							simulation.PC=simulation.registers[dest]+literal;
							simulation.offset=simulation.PC-20000;
							
							stage[1].instruction="";
							stage[1].op1="";
							stage[1].op2="";
							stage[1].op3="";
							stage[0].instruction="";
							stage[0].op1="";
							stage[0].op2="";
							stage[0].op3="";
							
							try {
								reader.close();
								reader=new BufferedReader(new FileReader(filename));
							
							for(int j=0;j<simulation.offset;j++)
								reader.readLine();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return;
						}
						if(stage[2].instruction.matches("BZ"))
						{
							int temp=Integer.valueOf(stage[3].op1.charAt(1))-'0';
							if(simulation.registers[temp]==0)
							{
								literal=Integer.valueOf(stage[2].op1);
								if(literal>0)
									simulation.PC=simulation.PC+literal;
								if(literal<0)
									simulation.PC=simulation.PC-literal;
								simulation.offset=simulation.PC-20000;
							
								stage[1].instruction="";
								stage[1].op1="";
								stage[1].op2="";
								stage[1].op3="";
								stage[0].instruction="";
								stage[0].op1="";
								stage[0].op2="";
								stage[0].op3="";
							
								try {
									reader.close();
									reader=new BufferedReader(new FileReader(filename));
							
									for(int j=0;j<simulation.offset-2;j++)
										reader.readLine();
								} catch (IOException e) {
								// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return;
							}
								
						}
						if(stage[2].instruction.matches("BNZ"))
						{
							int temp=Integer.valueOf(stage[3].op1.charAt(1))-'0';
							if(simulation.registers[temp]!=0)
							{
								literal=Integer.valueOf(stage[2].op1);
							//	if(literal>0)
									simulation.PC=simulation.PC+literal;
								//if(literal<0)
									//simulation.PC=simulation.PCliteral;
								simulation.offset=simulation.PC-20000;
							
								stage[1].instruction="";
								stage[1].op1="";
								stage[1].op2="";
								stage[1].op3="";
								stage[0].instruction="";
								stage[0].op1="";
								stage[0].op2="";
								stage[0].op3="";
							
								try {
									reader.close();
									reader=new BufferedReader(new FileReader(filename));
							
									for(int j=0;j<simulation.offset-2;j++)
										reader.readLine();
								} catch (IOException e) {
								// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return;
							}
								
						}
						System.out.println("Execute stage:\t"+stage[2].instruction+" "+stage[2].op1+" "+stage[2].op2+" "+stage[2].op3);
						valid[dest]=0;
						decode(reader);
					}
					else
						return;
				}
				
			else
				return;
		
	}
	
	static void mem(String filename)
	{
		stage[3].instruction=stage[2].instruction;
		stage[3].op1=stage[2].op1;
		stage[3].op2=stage[2].op2;
		stage[3].op3=stage[2].op3;
		if(stage[3].instruction!=null)
		{
		if(stage[3].instruction.equals("LOAD"))
		{
			int dest=Integer.valueOf(stage[3].op1.charAt(1))-'0';
			int src1=Integer.valueOf(stage[3].op2.charAt(1))-'0';
			int src2;
			int literal=0;
			if(stage[3].op3.startsWith("R"))
				src2=Integer.valueOf(stage[3].op3.charAt(1))-'0';
			else
				literal=Integer.valueOf(stage[3].op3);
			simulation.registers[dest]=simulation.memory[(simulation.registers[src1]+literal)];
		}
		if(stage[3].instruction.equals("STORE"))
		{
			int dest=Integer.valueOf(stage[3].op1.charAt(1))-'0';
			int src1=Integer.valueOf(stage[3].op2.charAt(1))-'0';
			int src2;
			int literal=0;
			if(stage[3].op3.startsWith("R"))
				src2=Integer.valueOf(stage[3].op3.charAt(1))-'0';
			else
				literal=Integer.valueOf(stage[3].op3);
			simulation.memory[(simulation.registers[src1]+literal)]=simulation.registers[dest];
		}
		System.out.println("Mem stage:\t"+stage[3].instruction+" "+stage[3].op1+" "+stage[3].op2+" "+stage[3].op3);
	
		execute(filename);
		}
		else
			return;
	}
	
	static void writeBack(String filename)
	{
		stage[4].instruction=stage[3].instruction;
		stage[4].op1=stage[3].op1;
		stage[4].op2=stage[3].op2;
		stage[4].op3=stage[3].op3;
		//if(stage[4].instruction!=null && stage[4].instruction!="")
		if(stage[4].instruction!=null)
		{
		if(stage[4].instruction.equals("HALT"))
			System.exit(0);
			System.out.println("\nWriteback stage:"+stage[4].instruction+" "+stage[4].op1+" "+stage[4].op2+" "+stage[4].op3);
		if(!stage[4].instruction.matches("STORE|BAL|JUMP|BZ|BNZ")&&stage[4].instruction!="")
			valid[Integer.valueOf(stage[4].op1.charAt(1))-'0']=1;
		mem(filename);
		}
		else
			return;
	}
	
	static void simulate(String filename,int n)
	{
			int flag=0;
			simulation.cycle_count=0;
			while(simulation.cycle_count<n)
			{
				if(flag==0)
				{
					fetch(reader);
					simulation.cycle_count++;
					System.out.println("Cycle:"+simulation.cycle_count);
					if(simulation.cycle_count==n)
						break;
					decode(reader);
					simulation.cycle_count++;
					System.out.println("Cycle:"+simulation.cycle_count);
					if(simulation.cycle_count==n)
						break;
					execute(filename);
					simulation.cycle_count++;
					System.out.println("Cycle:"+simulation.cycle_count);
					if(simulation.cycle_count==n)
						break;
					mem(filename);
					simulation.cycle_count++;
					System.out.println("Cycle:"+simulation.cycle_count);
					if(simulation.cycle_count==n)
						break;
					writeBack(filename);
					simulation.cycle_count++;
					System.out.println("Cycle:"+simulation.cycle_count);
					if(simulation.cycle_count==n)
						break;
					flag=1;
				}
				if(flag==1)
				{
					writeBack(filename);
					simulation.cycle_count++;
					System.out.println("Cycle:"+simulation.cycle_count);
				}
			}
			System.out.println("\nArchitectural registers:");
			for (int i=0;i<8;i++)
				System.out.println("R"+i+":"+simulation.registers[i]);
			
	}
	
	static void display()
	{
		System.out.println("Pipeline stages:");
		//System.out.println("\nFetch\tDecode\tExecute\tMem\tWriteback");
		//for(int i=0;i<5;i++)
			System.out.println("Fetch: "+stage[0].instruction);
			System.out.println("Decode: "+stage[1].instruction+stage[1].op1+stage[1].op2+stage[1].op3);
			System.out.println("Execute: "+stage[2].instruction+stage[2].op1+stage[2].op2+stage[2].op3);
			System.out.println("Mem: "+stage[3].instruction+stage[3].op1+stage[3].op2+stage[3].op3);
			System.out.println("Writeback: "+stage[4].instruction+stage[4].op1+stage[4].op2+stage[4].op3);
			System.out.println("Registers:");
			for(int i=0;i<8;i++)
				System.out.println("\t"+simulation.registers[i]);
			System.out.println("Memory:");
			for(int i=0;i<100;i++)
				System.out.println("\t"+simulation.memory[i]);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));
		String filename= args[0];
		reader = new BufferedReader(new FileReader(filename));
		FileReader filereader= new FileReader(filename);
		while(true)
		{
		System.out.println("Enter command(Initialize,Simulate,Display,Exit):");
				
		
			String input = new String(reader1.readLine());
			String[] inputtoken=input.split("\\s");
			
				if(inputtoken[0].equalsIgnoreCase("Initialize"))
						initialize();
				else if(inputtoken[0].equalsIgnoreCase("Simulate"))
				{
						int n=Integer.valueOf(inputtoken[1]);
						simulate(filename,n);
						System.out.println("\nSimulation completed for "+cycle_count+" cycles");
				}
				else if(input.equalsIgnoreCase("Display"))
						display();
				else if(input.equalsIgnoreCase("Exit"))
						System.exit(0);
				else
						System.out.println("Please enter valid command");
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
