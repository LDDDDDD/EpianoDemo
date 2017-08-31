package com.epiano.commutil;

public class Complex {
	public double real;		//复数实部
	public double image;	//复数虚部

	public Complex() {
		// TODO Auto-generated constructor stub
		this.real = 0;
		this.image = 0;
	}

	public Complex(double real, double image){
		this.real = real;
		this.image = image;
	}

	public Complex(int real, int image) {
		Integer integer = real;
		this.real = integer.floatValue();
		integer = image;
		this.image = integer.floatValue();
	}

	public Complex(double real) {
		this.real = real;
		this.image = 0;
	}

	// 复数乘法(a+bi)(c+di)=(ac-bd)+(bc+ad)i
	public Complex cc(Complex complex) {
		Complex tmpComplex = new Complex();
		tmpComplex.real = this.real * complex.real - this.image * complex.image;
		tmpComplex.image = this.real * complex.image + this.image * complex.real;
		return tmpComplex;
	}

	// 复数加法 (a+bi)+(c+di)=(a+c)+(b+d)i
	public Complex sum(Complex complex) {
		Complex tmpComplex = new Complex();
		tmpComplex.real = this.real + complex.real;
		tmpComplex.image = this.image + complex.image;
		return tmpComplex;
	}

	// 复数减法
	public Complex cut(Complex complex) {
		Complex tmpComplex = new Complex();
		tmpComplex.real = this.real - complex.real;
		tmpComplex.image = this.image - complex.image;
		return tmpComplex;
	}

	// 复数的模  复数z的模|z|=√a2+b2
	public int getIntValue(){
		int ret = 0;
		//ret = (int) Math.round(Math.sqrt(this.real*this.real - this.image*this.image));
		ret = (int) Math.round(Math.sqrt(this.real*this.real + this.image*this.image));
		return ret;
	}

	// 复数的模  复数z的模|z|=√a2+b2
	public float getFloatValue(){
		float ret;
		//ret = (int) Math.round(Math.sqrt(this.real*this.real - this.image*this.image));
		//ret = (float)(Math.sqrt(this.real*this.real - this.image*this.image));
		ret = (float)(Math.sqrt(this.real*this.real + this.image*this.image));
		return ret;
	}
}
