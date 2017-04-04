package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;




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
    private void optimizedMethod(ClassGen cgen, ConstantPoolGen cpgen, Method m){
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
            //******如果找到unary或者binary运算字节码，则运算出结果
            InstructionHandle change = handle;
            handle = handle.getNext();


            }else if(handle.getInstruction() instanceof StoreInstruction){
                //******如果找到store相关的字节码，则往后找相应的load字节码，并且把load替换成push,把之前的store和push删掉
                InstructionHandle change = handle;
                handle = handle.getNext();

            }else if(handle.getInstruction() instanceof IINC){
                //********如果遇到增量字节码，则转换成普通的字节码（iinc，iinc_w）
                InstructionHandle change = handle;
                handle = handle.getNext();

            }else{
                //*********向下遍历
                InstructionHandle change = handle;
                handle = handle.getNext();
            }
        }

        list.setPositions(true);
        mg.setMaxStack();
        mg.setMaxLocals();
        Method myMethod = mg.getMethod();
        cgen.replaceMethod(m, myMethod);
    }

    private Number getValue(InstructionList list, InstructionHandle handle){
        InstructionHandle myHandle = handle;

        //get values from bipush, sipush, ldc and ldc2_w
        //get values from integer, float, long and double constant
        //get values from integer add, mul, div and sub
        if (myHandle.getInstruction() instanceof BIPUSH){
             Number value = ((BIPUSH) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof SIPUSH){
             Number value = ((SIPUSH) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof LDC){
             Number value = ((LDC) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof LDC2_W){
             Number value = ((LDC2_W) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof ICONST){
             Number value = ((ICONST) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof FCONST){
             Number value = ((FCONST) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof LCONST){
            Number value = ((LCONST) myHandle.getInstruction()).getValue();
            deleteInstruction(list, myHandle);
            return value;
        }else if (myHandle.getInstruction() instanceof DCONST){
            Number value = ((DCONST) myHandle.getInstruction()).getValue();
            deleteInstruction(list, myHandle);
            return value;
        }else if (myHandle.getInstruction() instanceof IADD){

        }
    }

    private void deleteInstruction(InstructionList list, InstructionHandle handle){
        list.redirectBranches(handle, handle.getPrev());
        list.delete(handle);
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