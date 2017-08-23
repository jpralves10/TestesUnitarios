package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersCustomizados.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersCustomizados.ehHoje;
import static br.ce.wcaquino.matchers.MatchersCustomizados.ehHojeComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value={LocacaoService.class, DataUtils.class})
public class LocacaoServiceTest_PowerMock {

	private static Usuario usuario;
	private static Filme filme;

	@InjectMocks
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
	}
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		service = PowerMockito.spy(service);
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		List<Filme> filmes = Arrays.asList(filme);
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(25, 8, 2017));
		
		Locacao locacao = service.alugarFilme(usuario, filmes);

		error.checkThat(locacao.getValor(), is(equalTo(4.0)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception{
		List<Filme> filmes = Arrays.asList(filme);
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(26, 8, 2017));
		
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		assertThat(locacao.getDataRetorno(), caiNumaSegunda());
		PowerMockito.verifyNew(Date.class, Mockito.times(2)).withNoArguments();
	}
	
	@Test
	public void deveAlugarFilme_SemCalcularValor() throws Exception{
		List<Filme> filmes = Arrays.asList(filme);
		
		PowerMockito.doReturn(1.0).when(service, "calcularValorLocacao", filmes);
		
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		assertThat(locacao.getValor(), is(1.0));
		PowerMockito.verifyPrivate(service).invoke("calcularValorLocacao", filmes);
	}
}
