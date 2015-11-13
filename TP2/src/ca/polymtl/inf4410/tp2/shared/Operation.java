package ca.polymtl.inf4410.tp2.shared;

import java.io.Serializable;

public class Operation implements Serializable {
	public String name;
	public int operand;

	public Operation(String nameParam, int operandParam){
		name = nameParam;
		operand = operandParam;
	}
}
