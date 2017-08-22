package br.ce.wcaquino.servicos;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ce.wcaquino.exceptions.DividirPorZeroException;

public class CalculadoraTest {
	
	CalculadoraService calc;
	
	@Before
	public void setup(){
		calc = new CalculadoraService();
	}

	@Test
	public void somarDoisValores(){
		int a = 5;
		int b = 3;
		
		int resultado = calc.somar(a, b);
		
		Assert.assertEquals(8, resultado);
	}
	
	@Test
	public void subtrairDoisValores(){
		int a = 8;
		int b = 5;
		
		int resultado = calc.subtrair(a, b);
		
		Assert.assertEquals(3, resultado);
	}
	
	@Test
	public void dividirDoisValores() throws DividirPorZeroException{
		int a = 6;
		int b = 3;
		
		int resultado = calc.dividir(a, b);
		
		Assert.assertEquals(2, resultado);
	}
	
	@Test
	public void dividirDoisValoresString() throws DividirPorZeroException {
		String a = "6";
		String b = "3";
		
		int resultado = calc.dividir(a, b);
		
		Assert.assertEquals(2, resultado);
	}
	
	@Test(expected = DividirPorZeroException.class)
	public void excecaoAoDividirPorZero() throws DividirPorZeroException{
		int a = 10;
		int b = 0;
		
		int resultado = calc.dividir(a, b);
		
		Assert.assertEquals(2, resultado);
	}
}
