package com.duarte.serviceapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duarte.serviceapp.R;
import com.duarte.serviceapp.adapter.AdapterServico;
import com.duarte.serviceapp.helper.ConfiguracaoFirebase;
import com.duarte.serviceapp.helper.UsuarioFirebase;
import com.duarte.serviceapp.listener.RecyclerItemClickListener;
import com.duarte.serviceapp.model.Prestador;
import com.duarte.serviceapp.model.Servico;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;

public class PrestadorActivityDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerServicos;
    private AdapterServico adapterServico;
    private List<Servico> servicos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private FirebaseUser idat;

    private String idUsuarioLogado;
    private String urlImagem;
    private String nomePrestador;
    private String emailPrestador;
    private Uri fotoPrestador;
    private TextView nomeAtual;
    private TextView emailAtual;
    private ImageView fotoAtual;
    private ImageView image;

    //Drawer
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestador_drawer);

        botaoFlutuante();

        //Inicia os componentes
        drawer = findViewById(R.id.drawerlayout2);
        navigationView = findViewById(R.id.navView);
        View viewUser = navigationView.getHeaderView(0);
        View viewEmail = navigationView.getHeaderView(0);
        View viewFoto = navigationView.getHeaderView(0);

        emailAtual = viewEmail.findViewById(R.id.emailuser);
        nomeAtual = viewUser.findViewById(R.id.nomeuser);
        fotoAtual = viewFoto.findViewById(R.id.fotouser);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        recyclerServicos = findViewById(R.id.recyclerServicos);

        //Recupera serviços do prestador
        DatabaseReference servicosRef = firebaseRef
                .child("servicos")
                .child( idUsuarioLogado );

        //Recupera dados do prestador
        DatabaseReference prestadorRef = firebaseRef
                .child("prestadores")
                .child(idUsuarioLogado);

        //recupera dados autenticados
        idat = FirebaseAuth.getInstance().getCurrentUser();

        if (idat != null){
            String nomeLogado = idat.getDisplayName();
            String emailLogado = idat.getEmail();
           // Uri fotoURL = idat.getPhotoUrl();



            nomePrestador = nomeLogado;
            emailPrestador = emailLogado;
            //fotoPrestador = fotoURL;

        }
       // fotoAtual.setImageURI(fotoPrestador);
        emailAtual.setText(emailPrestador );

        servicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                servicos.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    servicos.add( ds.getValue(Servico.class) );
                }

                adapterServico.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        prestadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ){
                    Prestador prestador= dataSnapshot.getValue(Prestador.class);
                    nomePrestador = prestador.getNome();
                    nomeAtual.setText(nomePrestador);
                    String fURL = prestador.getUrlImagem();
                    Picasso.get().load(fURL)
                            .resize(300, 300)
                            .centerCrop()
                            .into(fotoAtual, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Bitmap imageBitmap = ((BitmapDrawable) fotoAtual.getDrawable()).getBitmap();
                                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                                    imageDrawable.setCircular(true);
                                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                    fotoAtual.setImageDrawable(imageDrawable);
                                }

                                @Override
                                public void onError(Exception e) {

                                }

                            });



                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //Configura recyclerView
        recyclerServicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerServicos.setHasFixedSize(true);
        adapterServico = new AdapterServico(servicos, this);
        recyclerServicos.setAdapter(adapterServico);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Serviços");
        setSupportActionBar(toolbar);

        //Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.open_drawer,R.string.close_drawer);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(PrestadorActivityDrawer.this);

        viewFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent config = new Intent(PrestadorActivityDrawer.this, PerfilPrestadorActivity.class);
                startActivity(config);

            }
        });


        //Adiciona o evento de clique no recyclerView
        clicRecicler();

    }

    //Drawer------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_home:{
                Intent home = new Intent(PrestadorActivityDrawer.this, PrestadorActivityDrawer.class);
                startActivity(home);
                break;
            }
            case R.id.menu_cont:{
                Toast.makeText(PrestadorActivityDrawer.this, "Em breve..", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.menu_ordem:{
                Intent ordem = new Intent(PrestadorActivityDrawer.this, OrdensServicoActivity.class);
                startActivity(ordem);
                break;
            }
            case R.id.menu_config:{
                Intent config = new Intent(PrestadorActivityDrawer.this, PerfilPrestadorActivity.class);
                startActivity(config);
                break;
            }
            case R.id.menu_sup:{
                Intent sup = new Intent(PrestadorActivityDrawer.this, SuporteActivityPrestador.class);
                startActivity(sup);
                break;
            }
            case R.id.menu_sair:{
                deslogarUsuario();
                break;
            }
        }

        drawer.closeDrawer(GravityCompat.START);


        return true;
    }


    //-------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_prestador, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair :
                deslogarUsuario();
                break;
            case R.id.menuConfiguracoes :
                abrirConfiguracoes();
                break;
            case R.id.menuNovoServico :
                abriNovoServico();
                break;
            case R.id.menuOrdensServico :
                abrirOrdensServico();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario(){
        try {
            autenticacao.signOut();
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(i);
            // finish();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void clicRecicler() {
        //Adiciona o evento de clique no recyclerView
        recyclerServicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerServicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Servico servicoSelecionado = servicos.get(position);
                                servicoSelecionado.remover();
                                Toast.makeText(PrestadorActivityDrawer.this,
                                        "Servico excluído com sucesso",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    private void abrirConfiguracoes() {
        startActivity(new Intent(PrestadorActivityDrawer.this, PerfilPrestadorActivity.class));
    }

    private void abriNovoServico() {
        startActivity(new Intent(PrestadorActivityDrawer.this, NovoServicoPrestadorActivity.class));
    }

    private void abrirOrdensServico() {
        startActivity(new Intent(PrestadorActivityDrawer.this, OrdensServicoActivity.class));
    }

    protected void botaoFlutuante(){
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fabSpeedDial);
        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //Toast.makeText(PrestadorActivity.this, "" + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                int id = menuItem.getItemId();

                if (id == R.id.menuConfiguracoes) {
                    startActivity(new Intent(PrestadorActivityDrawer.this, PerfilPrestadorActivity.class));
                    return true;
                }
                if (id == R.id.ordens) {
                    startActivity(new Intent(PrestadorActivityDrawer.this, OrdensServicoActivity.class));
                    return true;
                }
//                if (id == R.id.chat) {
//                    Toast.makeText(PrestadorActivityDrawer.this, "Em breve", Toast.LENGTH_SHORT).show();
//                    //startActivity(new Intent(PrestadorActivity.this, PerfilPrestadorActivity.class));
//                    return true;
//                }
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });
    }
}