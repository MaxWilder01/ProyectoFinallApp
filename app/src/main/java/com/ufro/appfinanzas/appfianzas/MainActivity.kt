package com.ufro.appfinanzas.appfianzas

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var transaccionesList: ArrayList<Transaccion>? = null
    private var mDatabaseTransacciones: DatabaseReference? = null
    private var mDatabaseTotales: DatabaseReference? = null
    private var adapter: TransaccionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mAuth = FirebaseAuth.getInstance()
        mDatabaseTransacciones = FirebaseDatabase.getInstance().reference.child("usuarios").child((mAuth.currentUser)!!.uid).child("transacciones")
        mDatabaseTotales = FirebaseDatabase.getInstance().reference.child("usuarios").child((mAuth.currentUser)!!.uid).child("totales")

        escucharTransacciones()

        recyclerViewMain.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        transaccionesList = ArrayList()

        adapter = TransaccionAdapter(transaccionesList as MutableList<Transaccion>)

        recyclerViewMain.adapter = adapter

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerViewMain.adapter as TransaccionAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerViewMain)

        btnAgregarIngresoMain.setOnClickListener(this)
        btnAgregarGastoMain.setOnClickListener(this)

        setearMes()
    }

    override fun onClick(view: View?) {
        val i = view!!.id

        when (i) {
            R.id.btnAgregarIngresoMain -> {
                abrirDialogoAgregar(true)
            }
            R.id.btnAgregarGastoMain -> {
                abrirDialogoAgregar(false)
            }
        }
    }

    private fun escucharTransacciones() {
        val escuchadorTransacciones = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = recyclerViewMain.adapter as TransaccionAdapter
                adapter.limpiar()
                var sumaIngresos = 0
                var sumaGastos = 0
                for (data in dataSnapshot.children) {
                    val transaccionData = data.getValue<Transaccion>(Transaccion::class.java)
                    val transaccion = transaccionData?.let { it } ?: continue

                    if (transaccion.tipo == "ingreso") {
                        sumaIngresos += transaccion.cantidad
                    } else {
                        sumaGastos += transaccion.cantidad
                    }

                    adapter.add(transaccion)
                    Log.e("msj", "onDataChange: Message data is updated: " + transaccion.toString())
                }
                val textoIngresos = "$ ${darFormatoNumero(sumaIngresos)}"
                val textoGastos = "$ ${darFormatoNumero(sumaGastos)}"
                //val textoSaldo = "$ ${darFormatoNumero(sumaIngresos - sumaGastos)}"

                txtCantidadIngresosMain.text = textoIngresos
                txtCantidadGastosMain.text = textoGastos
                //txtCantidadSaldoMain.text = textoSaldo

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        mDatabaseTransacciones!!.orderByChild("fecha").equalTo(getFecha()).addValueEventListener(escuchadorTransacciones)
        escucharSaldo()
    }

    private fun escucharSaldo() {
        mDatabaseTotales!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                val children = snapshot!!.children
                var saldo:Long = 0
                var contador = 0
                children.forEach {

                    if (contador == 0)
                        saldo-= it.value as Long
                    else
                        saldo+= it.value as Long
                    contador++
                }

                setearSaldo(saldo)
                calcularPresupuesto(saldo)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getFecha(): String {
        val now = Calendar.getInstance()
        val year = Integer.toString(now.get(Calendar.YEAR))
        val month = Integer.toString(now.get(Calendar.MONTH))

        return year + month
    }

    private fun abrirDialogoAgregar(opcion: Boolean) {
        val agregarIngresoDialog = AgregarIngreso()
        val agregarGastoDialog = AgregarGasto()

        if (opcion) {
            agregarIngresoDialog.show(supportFragmentManager, "Agregar Transaccion")
        } else {
            agregarGastoDialog.show(supportFragmentManager, "Agregar Gasto")
        }
    }

    private fun darFormatoNumero(numero: Int): String {
        return String.format(Locale.US, "%,d", numero).replace(',', '.')
    }

    private fun calcularPresupuesto(saldo: Long) {
        val ca1 = Calendar.getInstance()
        ca1.set(ca1.get(Calendar.YEAR), ca1.get(Calendar.MONTH), ca1.get(Calendar.DAY_OF_MONTH))
        ca1.minimalDaysInFirstWeek = 1
        val wk = ca1.get(Calendar.WEEK_OF_MONTH)


        val presupuesto = Integer.parseInt("${saldo/(5-wk)}")
        val textoPresupuesto = "$ ${darFormatoNumero(presupuesto)}"
        txtPresupuesto.text = textoPresupuesto
    }

    private fun setearSaldo(saldo: Long) {
        val textoSaldo = "$ ${darFormatoNumero(Integer.parseInt("" + saldo))}"
        txtCantidadSaldoMain.text = textoSaldo
    }

    private fun setearMes() {
        val now = Calendar.getInstance()
        val month = (now.get(Calendar.MONTH))
        val x = arrayOf(
                "enero", "febrero", "marzo", "abril", "mayo",
                "junio", "julio", "agosto", "septiembre",
                "octubre", "noviembre", "diciembre")

        val textoUltMov = "Movimientos de ${x[month]}"
        txtUltMov.text =  textoUltMov
    }
}
