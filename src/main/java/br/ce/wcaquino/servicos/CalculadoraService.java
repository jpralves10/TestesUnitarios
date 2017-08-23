package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.DividirPorZeroException;

public class CalculadoraService {

	public int somar(int a, int b) {
		return a + b;
	}

	public int subtrair(int a, int b) {
		return a - b;
	}

	public int dividir(int a, int b) throws DividirPorZeroException {
		if(b == 0)
			throw new DividirPorZeroException();
		
		return a / b;
	}
	
	public int dividir(String a, String b) throws DividirPorZeroException {
		if(Integer.parseInt(b) == 0)
			throw new DividirPorZeroException();
		
		return Integer.parseInt(a) / Integer.parseInt(b);
	}
	
	public void imprime(){
		System.out.println("Passei aqui");
	}
}
