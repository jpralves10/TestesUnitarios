package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersCustomizados.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersCustomizados.ehHoje;
import static br.ce.wcaquino.matchers.MatchersCustomizados.ehHojeComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterData;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.runners.ParallelRunner;

@RunWith(ParallelRunner.class)
public class LocacaoServiceTest {

	private static Usuario usuario;
	private static Filme filme;
	private static Locacao locacao;

	@InjectMocks @Spy
	private LocacaoService service;

	@Mock
	private LocacaoDAO locacaoDAO;
	@Mock
	private SPCService spcService;
	@Mock
	private EmailService emailService;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void setupClass(){
		usuario = umUsuario().agora();
		filme = umFilme().agora();
		locacao = umaLocacao().agora();
	}
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		List<Filme> filmes = Arrays.asList(filme);
		
		Mockito.doReturn(obterData(25, 8, 2017)).when(service).obterData();
		
		Locacao locacao = service.alugarFilme(usuario, filmes);

		error.checkThat(locacao.getValor(), is(equalTo(4.0)));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), obterData(25, 8, 2017)), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterData(26, 8, 2017)), is(true));
	}
	
	@Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		
		List<Filme> filmes = Arrays.asList(umFilmeSemEstoque().agora());
		
		service.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException{
		List<Filme> filmes = Arrays.asList(filme);
		
		try {
			service.alugarFilme(null, filmes); //Forma Robusta
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário Inexistente"));
		}
	}
	
	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException{
		
		exception.expect(LocadoraException.class); //Forma Nova
		exception.expectMessage("Nenhum filme");
		
		service.alugarFilme(usuario, null);
	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception{
		List<Filme> filmes = Arrays.asList(filme);
		
		Mockito.doReturn(obterData(26, 8, 2017)).when(service).obterData();
		
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		assertThat(locacao.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception{
		List<Filme> filmes = Arrays.asList(filme);
		
		//Usuario usuario2 = umUsuario().comNome("Usuario 2").agora();
		
		when(spcService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);
		
		try {
			service.alugarFilme(usuario, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			Assert.assertThat(e.getMessage(), is("Usuário Negativado"));
		}
		
		verify(spcService).possuiNegativacao(usuario);
	}
	
	@Test
	public void deveTratarErroDeConexaoComSPC() throws Exception{
		List<Filme> filmes = Arrays.asList(filme);
		
		when(spcService.possuiNegativacao(usuario)).thenThrow(new Exception("Falha de conexão"));
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Problemas de conexão, tente novamente");
		
		service.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void deveCalcularValorLocacao() throws Exception{
		List<Filme> filmes = Arrays.asList(filme);
		
		Class<LocacaoService> clazz = LocacaoService.class;
		Method metodo = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		metodo.setAccessible(true);
		Double valor = (Double) metodo.invoke(service, filmes);
		
		assertThat(valor, is(4.0));
	}
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas(){
		
		Usuario usuario1 = umUsuario().comNome("Usuario 1 (Atrasado)").agora();
		Usuario usuario2 = umUsuario().comNome("Usuario 2 (em Dia)").agora();
		Usuario usuario3 = umUsuario().comNome("Usuario 3 (Atrasado)").agora();
		
		List<Locacao> locacaos = Arrays.asList(umaLocacao().atrasada().comUsuario(usuario1).agora(),
											   umaLocacao().comUsuario(usuario2).agora(),
											   umaLocacao().atrasada().comUsuario(usuario3).agora(),
											   umaLocacao().atrasada().comUsuario(usuario3).agora());
		
		when(locacaoDAO.obterLocacoesPendentes()).thenReturn(locacaos);
		
		service.notificarAtrasos();
		
		verify(emailService, times(3)).notificarAtraso(Mockito.any(Usuario.class));
		verify(emailService).notificarAtraso(usuario1);
		verify(emailService, atLeastOnce()).notificarAtraso(usuario3); //Pelo menos um usuario3
		verify(emailService, never()).notificarAtraso(usuario2); //Verificação nunca deve acontecer (usuario em dia)
		verifyNoMoreInteractions(emailService);
		//verifyZeroInteractions(spcService); //Esta linha é desnecessária, spcService não é invocado em notificarAtraso()
	}
	
	@Test
	public void deveProrrogarUmaLocacao(){
		
		service.prorrogarLocacao(locacao, 3);
		
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(locacaoDAO).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDias(3));
	}
}
