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
        InstructionHandle firstHandle = list.getStart();
        
        while(firstHandle != null){
            if(firstHandle.getInstruction() instanceof IINC){
                int id = ((IINC) firstHandle.getInstruction()).getIndex();
                int inc = ((IINC) firstHandle.getInstruction()).getIncrement();
                
                list.insert(firstHandle, new BIPUSH((byte) inc));
                InstructionHandle bipush = handle.getPrev();
                list.insert(firstHandle, new ILOAD(id));
                list.insert(firstHandle, new IADD());
                list.insert(firstHandle, new ISTORE(id));
                deleteInstruction(list,firstHandle);
                firstHandle = bipush;
                list.setPositions();
            }else{
                firstHandle = firstHandle.getNext();
                list.setPositions();
            }
        }
        
        while (handle != null){
//            -1~5 iconst
//            -128 ~127 bipush
//            -32768 ~ 32767 sipush
//            -2147483648 ~ 2147483647 ldc (int, float and String)
//            ldc2_w (long and double)

            if (handle.getInstruction() instanceof ArithmeticInstruction){
                InstructionHandle change = handle;
                handle = handle.getNext();
                if(!(change.getPrev().getInstruction() instanceof LoadInstruction) && !(change.getPrev().getPrev().getInstruction() instanceof LoadInstruction)){
                    Number value = getValue(list, change, cpgen);
                    if (value instanceof Integer){
                        list.insert(handle, new LDC(cpgen.addInteger((int) value)));
                        list.setPositions();
                    }else if (value instanceof Float) {
                        list.insert(handle, new LDC(cpgen.addFloat((float) value)));
                        list.setPositions();
                    }else if (value instanceof Double) {
                        list.insert(handle, new LDC2_W(cpgen.addDouble((double) value)));
                        list.setPositions();
                    }else if (value instanceof Long) {
                        list.insert(handle, new LDC2_W(cpgen.addLong((long) value)));
                        list.setPositions();
                    }
                }
            }else if(handle.getInstruction() instanceof IFNE){
                InstructionHandle toHandle = handle;
                deleteInstruction(list, toHandle.getPrev());
                Number secondValue = getValue(list, toHandle.getPrev(), cpgen);
                Number firstValue = getValue(list, toHandle.getPrev(), cpgen);
                
                if(secondValue instanceof Long && firstValue instanceof Long){
                    long s = (long) secondValue;
                    long f = (long) firstValue;
                    if(s == f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }else if(secondValue instanceof Double && firstValue instanceof Double){
                    double s = (double) secondValue;
                    double f = (double) firstValue;
                    if(s == f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }else if(secondValue instanceof Float && firstValue instanceof Float) {
                    float s = (float) secondValue;
                    float f = (float) firstValue;
                    if(s == f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }
                handle = handle.getNext();
                deleteInstruction(list, toHandle);
                
            }else if(handle.getInstruction() instanceof IFLE){
                InstructionHandle toHandle = handle;
                deleteInstruction(list, toHandle.getPrev());
                Number secondValue = getValue(list, toHandle.getPrev(), cpgen);
                Number firstValue = getValue(list, toHandle.getPrev(), cpgen);
                
                if(secondValue instanceof Long && firstValue instanceof Long){
                    long s = (long) secondValue;
                    long f = (long) firstValue;
                    if(s <= f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }else if(secondValue instanceof Double && firstValue instanceof Double){
                    double s = (double) secondValue;
                    double f = (double) firstValue;
                    if(s <= f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }else if(secondValue instanceof Float && firstValue instanceof Float) {
                    float s = (float) secondValue;
                    float f = (float) firstValue;
                    if(s <= f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }
                handle = handle.getNext();
                deleteInstruction(list, toHandle);

            }else if(handle.getInstruction() instanceof IFGE){
                InstructionHandle toHandle = handle;
                deleteInstruction(list, toHandle.getPrev());
                Number secondValue = getValue(list, toHandle.getPrev(), cpgen);
                Number firstValue = getValue(list, toHandle.getPrev(), cpgen);
                
                if(secondValue instanceof Long && firstValue instanceof Long){
                    long s = (long) secondValue;
                    long f = (long) firstValue;
                    if(s >= f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }else if(secondValue instanceof Double && firstValue instanceof Double){
                    double s = (double) secondValue;
                    double f = (double) firstValue;
                    if(s >= f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }else if(secondValue instanceof Float && firstValue instanceof Float) {
                    float s = (float) secondValue;
                    float f = (float) firstValue;
                    if(s >= f){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list, toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                }
                handle = handle.getNext();
                deleteInstruction(list, toHandle);
                
            }else if(handle.getInstruction() instanceof IF_ICMPLE){
                InstructionHandle toHandle = handle;
                if(!(toHandle.getNext().getNext().getInstruction() instanceof GotoInstruction)){
                    handle = handle.getNext();
                }else{
                    int secondValue = (int) getValue(list, toHandle.getPrev(), cpgen);
                    int firstValue = (int) getValue(list, toHandle.getPrev(), cpgen);
                    if(secondValue <= firstValue){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list,toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                    handle = handle.getNext();
                    deleteInstruction(list, toHandle);
                }
                
            }else if(handle.getInstruction() instanceof IF_ICMPGE){
                InstructionHandle toHandle = handle;
                if(!(toHandle.getNext().getNext().getInstruction() instanceof GotoInstruction)){
                    handle = handle.getNext();
                }else{
                    int secondValue = (int) getValue(list, toHandle.getPrev(), cpgen);
                    int firstValue = (int) getValue(list, toHandle.getPrev(), cpgen);
                    if(secondValue >= firstValue){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list,toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                    handle = handle.getNext();
                    deleteInstruction(list, toHandle);
                }

            }else if(handle.getInstruction() instanceof IF_ICMPNE){
                InstructionHandle toHandle = handle;
                if(!(toHandle.getNext().getNext().getInstruction() instanceof GotoInstruction)){
                    handle = handle.getNext();
                }else{
                    int secondValue = (int) getValue(list, toHandle.getPrev(), cpgen);
                    int firstValue = (int) getValue(list, toHandle.getPrev(), cpgen);
                    if(secondValue == firstValue){
                        deleteInstruction(list, toHandle.getNext().getNext());
                        deleteInstruction(list,toHandle.getNext().getNext());
                    }else{
                        deleteInstruction(list, toHandle.getNext());
                        deleteInstruction(list, toHandle.getNext());
                    }
                    handle = handle.getNext();
                    deleteInstruction(list, toHandle);
                }
                
            }else if(handle.getInstruction() instanceof ConversionInstruction){
                InstructionHandle toHandle = handle;
                Number toChange = convert(toHandle,getValue(list, toHandle.getPrev(), cpgen));
                
                if (toChange instanceof Integer){
                    list.insert(toHandle, new LDC(cpgen.addInteger((int) toChange)));
                    list.setPositions();
                }else if (toChange instanceof Float) {
                    list.insert(toHandle, new LDC(cpgen.addFloat((float) toChange)));
                    list.setPositions();
                }else if (toChange instanceof Double) {
                    list.insert(toHandle, new LDC2_W(cpgen.addDouble((double) toChange)));
                    list.setPositions();
                }else if (toChange instanceof Long) {
                    list.insert(toHandle, new LDC2_W(cpgen.addLong((long) toChange)));
                    list.setPositions();
                }
                handle = toHandle.getPrev();
                deleteInstruction(list, toHandle);
                
            }else if(handle.getInstruction() instanceof StoreInstruction){
                
                InstructionHandle change = handle;
                if(change.getPrev().getInstruction() instanceof ArithmeticInstruction || change.getPrev().getInstruction() instanceof LoadInstruction){
                    handle = handle.getNext();
                }else{
                    if(handle.getNext().getNext().getNext().getInstruction() instanceof IfInstruction && !(handle.getNext().getNext().getNext().getNext().getNext().getInstruction() instanceof GotoInstruction)){
                        handle = handle.getNext();
                    }else{
                        if(change.getInstruction() instanceof ISTORE){
                            int id = ((ISTORE) change.getInstruction()).getIndex();
                            int value = (int) getValue(list, change.getPrev(), cpgen);
                            
                            InstructionHandle toHandle = change.getNext();
                            InstructionHandle nextHandle = toHandle;
                            
                            if(!(nextHandle.getInstruction() instanceof ILOAD)){
                                handle = nextHandle;
                            }else{
                                while((nextHandle.getInstruction() instanceof ILOAD)){
                                    nextHandle = nextHandle.getNext();
                                    handle = nextHandle;
                                }
                            }
                            deleteInstruction(list, change);
                            
                            while (toHandle != null){
                                int i;
                                if(toHandle.getInstruction() instanceof ILOAD){
                                    i = ((ILOAD) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        if (value > 32767 || value < -32768) {
                                            list.insert(toHandle, new LDC(cpgen.addInteger(value)));
                                        } else if (value < -128 || value > 127) {
                                            list.insert(toHandle, new SIPUSH((short) value));
                                        } else {
                                            list.insert(toHandle, new BIPUSH((byte) value));
                                        }
                                        
                                        InstructionHandle toDelete = toHandle;
                                        toHandle = toHandle.getNext();
                                        deleteInstruction(list, toDelete);
                                        list.setPositions();
                                    }
                                }else if(toHandle.getInstruction() instanceof ISTORE){
                                    i = ((ISTORE) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        break;
                                    }
                                }
                                toHandle = toHandle.getNext();
                            }
                        }else if(handle.getInstruction() instanceof DSTORE){
                            int id = ((DSTORE) change.getInstruction()).getIndex();
                            double value = (double) getValue(list, change.getPrev(), cpgen);
                            deleteInstruction(list, change.getPrev());
                            InstructionHandle toHandle = change.getNext();
                            InstructionHandle nextHandle = toHandle;
                            
                            if(!(nextHandle.getInstruction() instanceof DLOAD)){
                                handle = nextHandle;
                            }else{
                                while((nextHandle.getInstruction() instanceof DLOAD)){
                                    nextHandle = nextHandle.getNext();
                                    handle = nextHandle;
                                }
                            }
                            deleteInstruction(list, change);
                            
                            while (toHandle != null){
                                int i;
                                if(toHandle.getInstruction() instanceof DLOAD){
                                    i = ((DLOAD) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        list.insert(toHandle, new LDC2_W(cpgen.addDouble((double) value)));
                                        InstructionHandle toDelete = toHandle;
                                        toHandle = toHandle.getNext();
                                        deleteInstruction(list, toDelete);
                                        list.setPositions();
                                    }
                                }else if(toHandle.getInstruction() instanceof DSTORE){
                                    i = ((DSTORE) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        break;
                                    }
                                }
                                toHandle = toHandle.getNext();
                            }
                        }else if(handle.getInstruction() instanceof FSTORE){
                            int id = ((FSTORE) change.getInstruction()).getIndex();
                            float value = (float) getValue(list, change.getPrev(), cpgen);
                            deleteInstruction(list, change.getPrev());
                            InstructionHandle toHandle = change.getNext();
                            InstructionHandle nextHandle = toHandle;
                            
                            if(!(nextHandle.getInstruction() instanceof FLOAD)){
                                handle = nextHandle;
                            }else{
                                while((nextHandle.getInstruction() instanceof FLOAD)){
                                    nextHandle = nextHandle.getNext();
                                    handle = nextHandle;
                                }
                            }
                            
                            deleteInstruction(list, change);
                            
                            while (toHandle != null){
                                int i;
                                if(toHandle.getInstruction() instanceof FLOAD){
                                    i = ((FLOAD) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        list.insert(toHandle, new LDC(cpgen.addFloat((float) value)));
                                        InstructionHandle toDelete = toHandle;
                                        toHandle = toHandle.getNext();
                                        deleteInstruction(list, toDelete);
                                        list.setPositions();
                                    }
                                }else if(toHandle.getInstruction() instanceof FSTORE){
                                    i = ((FSTORE) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        break;
                                    }
                                }
                                toHandle = toHandle.getNext();
                            }
                        }else if(handle.getInstruction() instanceof LSTORE){
                            int id = ((LSTORE) change.getInstruction()).getIndex();
                            long value = (long) getValue(list, change.getPrev(), cpgen);
                            deleteInstruction(list, change.getPrev());
                            InstructionHandle toHandle = change.getNext();
                            InstructionHandle nextHandle = toHandle;
                            
                            if(!(nextHandle.getInstruction() instanceof LLOAD)){
                                handle = nextHandle;
                            }else{
                                while((nextHandle.getInstruction() instanceof LLOAD)){
                                    nextHandle = nextHandle.getNext();
                                    handle = nextHandle;
                                }
                            }
                            deleteInstruction(list, change);
                            
                            while (toHandle != null){
                                int i;
                                if(toHandle.getInstruction() instanceof LLOAD){
                                    i = ((LLOAD) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        list.insert(toHandle, new LDC2_W(cpgen.addLong((long) value)));
                                        InstructionHandle toDelete = toHandle;
                                        toHandle = toHandle.getNext();
                                        deleteInstruction(list, toDelete);
                                        list.setPositions();
                                    }
                                }else if(toHandle.getInstruction() instanceof LSTORE){
                                    i = ((LSTORE) toHandle.getInstruction()).getIndex();
                                    if(i == id){
                                        break;
                                    }
                                }
                                toHandle = toHandle.getNext();
                            }
                        }
                    }
                }
                                   
            }else{
                handle = handle.getNext();
                list.setPositions();
            }
        }
        
        InstructionHandle check = list.getStart();
        while(check != null){
            if(check.getInstruction() instanceof IF_ICMPGE || check.getInstruction() instanceof IF_ICMPLE || check.getInstruction() instanceof IF_ICMPNE){
                InstructionHandle findGoto = check;
                while(!(findGoto.getInstruction() instanceof GotoInstruction)){
                    if(findGoto.getNext().getInstruction() instanceof GotoInstruction){
                        IfInstruction change = (IfInstruction)check.getInstruction();
                        change.setTarget(findGoto.getNext().getNext());
                        list.setPositions();
                    }
                    findGoto = findGoto.getNext();
                }
            }
            check = check.getNext();
        }
        
        list.setPositions(true);

        mg.setMaxStack();
        mg.setMaxLocals();
        Method myMethod = mg.getMethod();
        cgen.replaceMethod(m, myMethod);
    }

    private Number getValue(InstructionList list, InstructionHandle handle, ConstantPoolGen cpgen){
        InstructionHandle myHandle = handle;

        //get values from bipush, sipush, ldc and ldc2_w
        //get values from integer, float, long and double constant
        //get values from add, mul, div and sub
        if (myHandle.getInstruction() instanceof BIPUSH){
             Number value = ((BIPUSH) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof SIPUSH){
             Number value = ((SIPUSH) myHandle.getInstruction()).getValue();
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof LDC){
             Number value = (Number) ((LDC) myHandle.getInstruction()).getValue(cpgen);
             deleteInstruction(list, myHandle);
             return value;
        }else if (myHandle.getInstruction() instanceof LDC2_W){
             Number value = (Number) ((LDC2_W) myHandle.getInstruction()).getValue(cpgen);
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
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (int) firstValue + (int) secondValue;
        }else if (myHandle.getInstruction() instanceof ISUB){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (int) firstValue - (int) secondValue;
        }else if (myHandle.getInstruction() instanceof IMUL){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (int) firstValue * (int) secondValue;
        }else if (myHandle.getInstruction() instanceof IDIV){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (int) firstValue / (int) secondValue;
        }else if (myHandle.getInstruction() instanceof FADD){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (float) firstValue + (float) secondValue;
        }else if (myHandle.getInstruction() instanceof FSUB){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (float) firstValue - (float) secondValue;
        }else if (myHandle.getInstruction() instanceof FSUB){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (float) firstValue - (float) secondValue;
        }else if (myHandle.getInstruction() instanceof FMUL){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (float) firstValue * (float) secondValue;
        }else if (myHandle.getInstruction() instanceof FDIV){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (float) firstValue / (float) secondValue;
        }else if (myHandle.getInstruction() instanceof LADD){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (long) firstValue + (long) secondValue;
        }else if (myHandle.getInstruction() instanceof LSUB){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (long) firstValue - (long) secondValue;
        }else if (myHandle.getInstruction() instanceof LMUL){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (long) firstValue * (long) secondValue;
        }else if (myHandle.getInstruction() instanceof LDIV){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (long) firstValue / (long) secondValue;
        }else if (myHandle.getInstruction() instanceof DADD){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (double) firstValue + (double) secondValue;
        }else if (myHandle.getInstruction() instanceof DSUB){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (double) firstValue - (double) secondValue;
        }else if (myHandle.getInstruction() instanceof DMUL){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (double) firstValue * (double) secondValue;
        }else if (myHandle.getInstruction() instanceof DDIV){
            Number secondValue = getValue(list, myHandle.getPrev(), cpgen);
            Number firstValue = getValue(list, myHandle.getPrev(), cpgen);
            deleteInstruction(list, myHandle);
            return (double) firstValue / (double) secondValue;
        }
        return null;
    }

    private Number convert(InstructionHandle handle, Number toChange){
        //integer to float, double and long
        //float to ...
        //double to ...
        //long to ...
        if (handle.getInstruction() instanceof I2F){
            return (float)(int) toChange;
        }else if (handle.getInstruction() instanceof I2D){
            return (double)(int) toChange;
        }else if (handle.getInstruction() instanceof I2L){
            return (long)(int) toChange;
        }else if (handle.getInstruction() instanceof F2I){
            return (int)(float) toChange;
        }else if (handle.getInstruction() instanceof F2D){
            return (double)(float) toChange;
        }else if (handle.getInstruction() instanceof F2L){
            return (long)(float) toChange;
        }else if (handle.getInstruction() instanceof D2I){
            return (int)(double) toChange;
        }else if (handle.getInstruction() instanceof D2F){
            return (float)(double) toChange;
        }else if (handle.getInstruction() instanceof D2L){
            return (long)(double) toChange;
        }else if (handle.getInstruction() instanceof L2I){
            return (int)(long) toChange;
        }else if (handle.getInstruction() instanceof L2D){
            return (double)(long) toChange;
        }else if (handle.getInstruction() instanceof L2F){
            return (float)(long) toChange;
        }
        return null;
    }

    private void deleteInstruction(InstructionList list, InstructionHandle handle){
        try{
            list.delete(handle);
        }catch (Exception targetLostException){

        }
    }

    //optimize all methods
	public void optimize()
	    {
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();
        cgen.setMajor(50);

		// Implement your optimization here
        Method[] methods = cgen.getMethods();
        for (Method m: methods){
        	optimizedMethod(cgen, cpgen, m);
		}

		this.optimized = cgen.getJavaClass();
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