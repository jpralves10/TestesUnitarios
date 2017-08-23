package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static br.ce.wcaquino.utils.DataUtils.verificarDiaSemana;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDAO locacaoDAO;
	private SPCService spcService;
	private EmailService emailService;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		
		if(usuario == null) {
			throw new LocadoraException("Usuário Inexistente");
		}
		
		if(filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Nenhum filme");
		}
		
		for(Filme filme : filmes){
			if(filme.getEstoque() == 0){
				throw new FilmeSemEstoqueException();
			}
		}
		
		boolean negativado;
		try {
			negativado = spcService.possuiNegativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas de conexão, tente novamente");
		}
		
		if(negativado){
			throw new LocadoraException("Usuário Negativado");
		}
		
		Locacao locacao = new Locacao();
		
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(obterData());
		locacao.setValor(calcularValorLocacao(filmes));

		//Entrega no dia seguinte
		Date dataEntrega = obterData();
		dataEntrega = adicionarDias(dataEntrega, 1);
		
		if(verificarDiaSemana(dataEntrega, Calendar.SUNDAY))
			dataEntrega = adicionarDias(dataEntrega, 1);
		
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		locacaoDAO.salvar(locacao);
				
		return locacao;
	}

	protected Date obterData() {
		return new Date();
	}

	private Double calcularValorLocacao(List<Filme> filmes) {
		Double valorTotal = 0d;
		int i = 0;
		for(Filme filme : filmes){
			Double valorFilme = filme.getPrecoLocacao();
			
			if(i == 2)
				valorFilme = valorFilme * 0.75;
			if(i == 3)
				valorFilme = valorFilme * 0.50;
			if(i == 4)
				valorFilme = valorFilme * 0.25;
			if(i == 5)
				valorFilme = valorFilme * 0.0;
			
			valorTotal += valorFilme; i++;
		}
		return valorTotal;
	}

	public void notificarAtrasos(){
		List<Locacao> locacoes = locacaoDAO.obterLocacoesPendentes();
		for(Locacao locacao: locacoes){
			if(locacao.getDataRetorno().before(obterData()))
				emailService.notificarAtraso(locacao.getUsuario());
		}
	}
	
	public void prorrogarLocacao(Locacao locacao, int dias){
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(obterData());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		locacaoDAO.salvar(novaLocacao);
	}
}