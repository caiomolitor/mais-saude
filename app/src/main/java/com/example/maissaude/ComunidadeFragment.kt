package com.example.maissaude

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import java.text.SimpleDateFormat

// --- Dados e APIs ---
data class FraseResponse(val q: String, val a: String)
data class TraducaoResponse(val responseData: ResponseData)
data class ResponseData(val translatedText: String)
data class ForecastResponse(val city: City, val list: List<ForecastItem>)
data class City(val name: String)
data class ForecastItem(val dt_txt: String, val main: Main, val weather: List<Weather>)
data class Main(val temp: Double)
data class Weather(val description: String)

interface MotivacionalApi {
    @GET("api/random")
    suspend fun getFrase(): Response<List<FraseResponse>>
}

interface MyMemoryApi {
    @GET("get")
    suspend fun traduzir(@Query("q") texto: String, @Query("langpair") langpair: String = "en|pt"): Response<TraducaoResponse>
}

interface ForecastApi {
    @GET("data/2.5/forecast?units=metric&lang=pt&appid=7959da654375612a4c69193f04ecc313")
    suspend fun getForecast(@Query("q") cidade: String): Response<ForecastResponse>
}

class ComunidadeFragment : Fragment() {

    private lateinit var textViewFrase: TextView
    private lateinit var textViewClima: TextView
    private lateinit var buttonAtualizar: Button
    private lateinit var buttonVoltar: Button
    private lateinit var buttonClimaCuritiba: Button
    private lateinit var buttonBuscarCidade: Button
    private lateinit var editTextCidade: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerEventos: RecyclerView
    private lateinit var db: DB

    // Admin
    private lateinit var adminLayout: LinearLayout
    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var editData: EditText
    private lateinit var editHora: EditText
    private lateinit var editLocal: EditText
    private lateinit var editTipo: EditText
    private lateinit var buttonAdicionar: Button
    private var isAdmin: Boolean = false

    private val apiZenQuotes by lazy {
        Retrofit.Builder()
            .baseUrl("https://zenquotes.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MotivacionalApi::class.java)
    }

    private val apiMyMemory by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyMemoryApi::class.java)
    }

    private val apiForecast by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForecastApi::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_comunidade, container, false)

        // --- Inicializa views ---
        textViewFrase = view.findViewById(R.id.textViewFraseMotivacional)
        textViewClima = view.findViewById(R.id.textViewClima)
        buttonAtualizar = view.findViewById(R.id.buttonAtualizar)
        buttonVoltar = view.findViewById(R.id.btnVoltarHome)
        buttonClimaCuritiba = view.findViewById(R.id.buttonClimaCuritiba)
        buttonBuscarCidade = view.findViewById(R.id.buttonBuscarCidade)
        editTextCidade = view.findViewById(R.id.editTextCidade)
        progressBar = view.findViewById(R.id.progressBarCarregando)
        recyclerEventos = view.findViewById(R.id.recyclerEventos)

        adminLayout = view.findViewById(R.id.adminEventosLayout)
        editNome = view.findViewById(R.id.editEventoNome)
        editDescricao = view.findViewById(R.id.editEventoDescricao)
        editData = view.findViewById(R.id.editEventoData)
        editLocal = view.findViewById(R.id.editEventoLocal)
        editTipo = view.findViewById(R.id.editEventoTipo)
        buttonAdicionar = view.findViewById(R.id.buttonAdicionarEvento)

        db = DB(requireContext())
        recyclerEventos.layoutManager = LinearLayoutManager(requireContext())
        recyclerEventos.isNestedScrollingEnabled = false

        // --- Verifica se é admin ---
        val prefs = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        isAdmin = email?.let { db.getTipoUsuario(it) == "admin" } ?: false
        adminLayout.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // --- Carrega dados iniciais ---
        carregarFraseMotivacional()
        carregarPrevisao("Curitiba")
        carregarEventos()

        // --- Botões ---
        buttonAtualizar.setOnClickListener {
            carregarFraseMotivacional()
            carregarPrevisao("Curitiba")
        }

        buttonClimaCuritiba.setOnClickListener { carregarPrevisao("Curitiba") }

        buttonBuscarCidade.setOnClickListener {
            val cidade = editTextCidade.text.toString().trim()
            if (cidade.isNotEmpty()) carregarPrevisao(cidade)
            else Toast.makeText(requireContext(), "Digite o nome de uma cidade", Toast.LENGTH_SHORT).show()
        }

        buttonVoltar.setOnClickListener { findNavController().navigateUp() }

        if (isAdmin) buttonAdicionar.setOnClickListener { adicionarEvento() }

        return view
    }

    // --- Frase Motivacional ---
    private fun carregarFraseMotivacional() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                textViewFrase.visibility = View.INVISIBLE

                val response = apiZenQuotes.getFrase()
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val frase = response.body()!![0].q
                    val autor = response.body()!![0].a
                    val fraseCompleta = "$frase\n— $autor"

                    val traducaoResponse = apiMyMemory.traduzir(fraseCompleta)
                    val traducao = traducaoResponse.body()?.responseData?.translatedText

                    textViewFrase.text = traducao ?: fraseCompleta

                    textViewFrase.alpha = 0f
                    textViewFrase.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(textViewFrase, "alpha", 0f, 1f).apply {
                        duration = 600
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                } else {
                    textViewFrase.text = "Motivação do dia indisponível."
                    textViewFrase.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                textViewFrase.text = "Motivação do dia não disponível."
                textViewFrase.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // --- Previsão do Tempo ---
    private fun carregarPrevisao(cidade: String) {
        lifecycleScope.launch {
            try {
                textViewClima.text = "Carregando previsão..."
                val response = apiForecast.getForecast(cidade)
                if (response.isSuccessful && response.body() != null) {
                    val forecast = response.body()!!
                    val nomeCidade = forecast.city.name

                    val previsoes = forecast.list.filter { it.dt_txt.contains("12:00:00") }
                        .distinctBy { it.dt_txt.substring(0, 10) }
                        .take(5)

                    val inputFormatter = SimpleDateFormat("yyyy-MM-dd")
                    val outputFormatter = SimpleDateFormat("dd/MM/yyyy")

                    val texto = StringBuilder("🌤️ Previsão do Tempo\n\nCidade: $nomeCidade\n\n")
                    for (item in previsoes) {
                        val dataOriginal = item.dt_txt.substring(0, 10)
                        val dataFormatada = try {
                            val date = inputFormatter.parse(dataOriginal)
                            outputFormatter.format(date)
                        } catch (e: Exception) {
                            dataOriginal
                        }
                        val descricao = item.weather.firstOrNull()?.description ?: ""
                        val temp = item.main.temp.toInt()
                        texto.append("$dataFormatada - $descricao, ${temp}°C\n")
                    }

                    textViewClima.text = texto.toString()
                    textViewClima.alpha = 0f
                    ObjectAnimator.ofFloat(textViewClima, "alpha", 0f, 1f).apply {
                        duration = 600
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                } else {
                    textViewClima.text = "Não foi possível carregar a previsão."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                textViewClima.text = "Erro ao obter previsão do tempo."
            }
        }
    }

    // --- Eventos ---
    private fun carregarEventos() {
        val listaEventos = db.listarEventos().toMutableList()
        recyclerEventos.adapter = ExcluirEventoAdapter(
            eventos = listaEventos,
            db = db,
            isAdmin = isAdmin
        ) {
            // Quando um evento for excluído, recarrega a lista
            carregarEventos()
        }
    }


    private fun adicionarEvento() {
        val nome = editNome.text.toString().trim()
        val descricao = editDescricao.text.toString().trim()
        val data = editData.text.toString().trim()
        val local = editLocal.text.toString().trim()
        val tipo = editTipo.text.toString().trim()

        if (nome.isEmpty() || descricao.isEmpty() || data.isEmpty() || local.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val evento = Evento(
            id = 0,
            nome = nome,
            descricao = descricao,
            data = data,
            hora = "",
            local = local,
            tipo = tipo
        )
        // função do DB
        val sucesso = db.inserirEvento(evento)
        if (sucesso) {
            Toast.makeText(requireContext(), "Evento adicionado!", Toast.LENGTH_SHORT).show()
            carregarEventos()
        } else {
            Toast.makeText(requireContext(), "Erro ao adicionar evento.", Toast.LENGTH_SHORT).show()
        }
    }
}
