package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;



public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	//main structure
    private optimizedMethod(ClassGen cgen, ConstantPoolGen cpgen, Method m){
        Code code = m.getCode();
        InstructionList list = new InstructionList(code.getCode());
        MethodGen mg = new MethodGen(m.getAccessFlags(), m.getReturnType(), m.getArgumentTypes(), null, m.getName(), cgen.getClassName(), list, cpgen);
        InstructionHandle handle = list.getStart();

        while (handle != null){
            //-1~5 iconst
            //-128 ~127 bipush
            //-32768 ~ 32767 sipush
            //-2147483648 ~ 2147483647 ldc (int, float and String)
            //ldc2_w (long and double)

            if (handle.getInstruction() instanceof ArithmeticInstruction){
            //unary and binary operators

            }else if(handle.getInstruction() instanceof StoreInstruction){


            }else if(handle.getInstruction() instanceof IINC){
                //i=i+1
                //iinc or iinc_w
                //up to 16bits[-32768,32767]


            }else{

            }
        }

        Method myMethod = mg.getMethod();
        Code myCode = myMethod.getCode();
        InstructionList myList = new InstructionList(myCode.getCode());
        cgen.replaceMethod(m, myMethod);
    }

    private Number getValue(){

    }



    //optimize all methods
	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
        Method[] methods = cgen.getMethods();
        for (Method m: methods){
        	optimizedMethod(cgen, cpgen, m);
		}

		this.optimized = gen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
}