package br.ce.wcaquino.builders;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static java.util.Arrays.asList;

import java.util.Date;

import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;

public class LocacaoBuilder {
	
	private Locacao locacao;
	
	private LocacaoBuilder(){}
	
	public static LocacaoBuilder umaLocacao(){
		LocacaoBuilder builder = new LocacaoBuilder();
		builder.locacao = new Locacao();
		builder.locacao.setDataLocacao(new Date());
		builder.locacao.setDataRetorno(obterDataComDiferencaDias(1));
		builder.locacao.setFilmes(asList(umFilme().agora()));
		builder.locacao.setUsuario(umUsuario().agora());
		builder.locacao.setValor(4.0);
		return builder;
	}
	
	public Locacao agora(){
		return locacao;
	}
	
	public LocacaoBuilder comDataRetorno(Date data) {
		locacao.setDataRetorno(data);
		return this;
	}
	
	public LocacaoBuilder comUsuario(Usuario usuario) {
		locacao.setUsuario(usuario);
		return this;
	}
	
	public LocacaoBuilder atrasada(){
		locacao.setDataLocacao(obterDataComDiferencaDias(-4));
		locacao.setDataRetorno(obterDataComDiferencaDias(-2));
		return this;
	}
}
