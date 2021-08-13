 package id.yongki.jonastrackingsystem;

 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;

 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;
 import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.Task;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.firestore.DocumentSnapshot;
 import com.google.firebase.firestore.FirebaseFirestore;
 import com.google.firebase.firestore.Query;
 import com.google.firebase.firestore.QueryDocumentSnapshot;
 import com.google.firebase.firestore.QuerySnapshot;

 import java.util.ArrayList;
 import java.util.Objects;

 import id.yongki.jonastrackingsystem.adapter.RecyclerAdapter;
 import id.yongki.jonastrackingsystem.model.UserModel;

 public class MainActivity extends AppCompatActivity implements RecyclerAdapter.OnItemListener {
     public static final String EMAIL = "id.yongki.jonastrackingsystem.MESSAGE";
     public static final String USERNAME = "id.yongki.jonastrackingsystem.MESSAGE";
     FirebaseAuth firebaseAuth;
     TextView label;
     ArrayList<UserModel> usersList = new ArrayList<>();
     RecyclerAdapter recyclerAdapter;
     LinearLayoutManager linearLayoutManager;
     FirebaseFirestore db = FirebaseFirestore.getInstance();
     DocumentSnapshot lastVisible;
     RecyclerView recyclerView;
     ProgressBar progressBar;
     boolean isScrolling;
     boolean isLastItemReached;
     private static final int PAGE_SIZE = 10;
     private SwipeRefreshLayout swipeRefreshLayout;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         linearLayoutManager = new LinearLayoutManager(this);
         firebaseAuth = FirebaseAuth.getInstance();
         recyclerAdapter = new RecyclerAdapter(getApplicationContext(), usersList, this);
         recyclerView = findViewById(R.id.myRecyclerview);
         recyclerView.setAdapter(recyclerAdapter);
         recyclerView.setLayoutManager(linearLayoutManager);
         progressBar = findViewById(R.id.list_progressbar);
         progressBar.setVisibility(View.VISIBLE);
         swipeRefreshLayout = findViewById(R.id.list_swiperefreshlayout);

         readData();
         swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 usersList.clear();
                 swipeRefreshLayout.setRefreshing(false);
                 readData();
             }
         });


     }

     private void readData() {
         db.collection("DataUsers")
                 .whereEqualTo("status", "aktif")
                 .orderBy("nama", Query.Direction.ASCENDING)
                 .limit(PAGE_SIZE)
                 .get()
                 .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                         if (task.isSuccessful()) {
                             if (task.getResult().size() < 1) {
                                 label.setVisibility(View.VISIBLE);
                                 progressBar.setVisibility(View.GONE);

                             } else {
                                 for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                                     usersList.add(new UserModel(
                                             (String) documentSnapshot.get("nama"),
                                             (String) documentSnapshot.get("username"),
                                             (String) documentSnapshot.get("email"),
                                             (String) documentSnapshot.get("nowa"),
                                             (String) documentSnapshot.get("jabatan"),
                                             (String) documentSnapshot.get("status")

                                     ));

                                 }
                                 progressBar.setVisibility(View.GONE);
                                 recyclerView.setAdapter(recyclerAdapter);
                                 lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                 RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                                     @Override
                                     public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                         super.onScrollStateChanged(recyclerView, newState);
                                         if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                             isScrolling = true;
                                             progressBar.setVisibility(View.VISIBLE);


                                         }
                                     }

                                     @Override
                                     public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                         super.onScrolled(recyclerView, dx, dy);
                                         int firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                                         int visibleItemCount = linearLayoutManager.getChildCount();
                                         int totalItemCount = linearLayoutManager.getItemCount();
                                         if (isScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                             isScrolling = false;
                                             db.collection("DataUsers")
                                                     .whereEqualTo("status", "aktif").orderBy("nama", Query.Direction.ASCENDING).startAfter(lastVisible).limit(10)
                                                     .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                     for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                                                         usersList.add(new UserModel(
                                                                 (String) documentSnapshot.get("nama"),
                                                                 (String) documentSnapshot.get("username"),
                                                                 (String) documentSnapshot.get("email"),
                                                                 (String) documentSnapshot.get("nowa"),
                                                                 (String) documentSnapshot.get("jabatan"),
                                                                 (String) documentSnapshot.get("status")

                                                         ));
                                                     }
                                                     progressBar.setVisibility(View.GONE);
                                                     recyclerAdapter.notifyDataSetChanged();

                                                     if (task.getResult().size() < PAGE_SIZE) {
                                                         isLastItemReached = true;
                                                     }
                                                     if (!isLastItemReached) {
                                                         lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                                     }
                                                 }
                                             });
                                         }
                                     }
                                 };

                                 recyclerView.addOnScrollListener(onScrollListener);
                             }
                         }

                     }

                 });
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_list, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         if (item.getItemId() == R.id.action_signout) {
             firebaseAuth.signOut();
             startActivity(new Intent(getApplicationContext(), LoginActivity.class));
             finish();
             return true;
         } else if (item.getItemId() == R.id.action_profile) {
             startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
         } else if (item.getItemId() == R.id.action_changepassword) {
             startActivity(new Intent(getApplicationContext(), LupaPasswordActivity.class));
         }
         return super.onOptionsItemSelected(item);
     }

     @Override
     public void onItemClick(int position) {
         Intent intent = new Intent(getApplicationContext(), DetailDriver.class);
         intent.putExtra(EMAIL, usersList.get(position).email);
         intent.putExtra(USERNAME, usersList.get(position).username);
         startActivity(intent);
     }
 }