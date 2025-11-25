package com.example.smartair.parent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.child.ChildDashboardHome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.PBManager;
import utils.PEFManager;
import utils.ZoneManager;

public class ParentChildrenFragment extends Fragment {

    private RecyclerView recyclerViewChildren;
    private TextView textViewNoChildren;
    private ProgressBar progressBar;
    private ChildrenAdapter childrenAdapter;
    private List<Map<String, Object>> childrenList;

    public ParentChildrenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_children, container, false);

        Button buttonAddChild = view.findViewById(R.id.buttonAddChild);
        Button buttonAddChildProfile = view.findViewById(R.id.buttonAddChildProfile);
        Button buttonEditPBs = view.findViewById(R.id.buttonEditPBs);
        Button buttonGoToChild = view.findViewById(R.id.buttonGoToChild);
        recyclerViewChildren = view.findViewById(R.id.recyclerViewChildren);
        textViewNoChildren = view.findViewById(R.id.textViewNoChildren);
        progressBar = view.findViewById(R.id.progressBar);

        childrenList = new ArrayList<>();
        childrenAdapter = new ChildrenAdapter(childrenList, new ChildrenAdapter.OnChildClickListener() {
            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmation(position);
            }
            @Override
            public void onClick(int position) {
                Map<String, Object> currentChild = childrenList.get(position);
                ChildIdManager manager = new ChildIdManager(requireContext());

                manager.SaveChildId((String) currentChild.get("uid"));

                Intent intent = new Intent(requireContext(), ChildDashboardHome.class);



                startActivity(intent);
                }
        });

        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChildren.setAdapter(childrenAdapter);
        buttonAddChildProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddChildActivity.class);
                getActivity().startActivityForResult(intent, 2);
            }
        });

        buttonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddChildAccountActivity.class);
                getActivity().startActivityForResult(intent, 1);
            }
        });

        Button buttonLinkProvider = view.findViewById(R.id.button2);
        buttonLinkProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ParentLinkGeneration.class);
                startActivity(intent);
            }
        });

        buttonEditPBs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPBsDialog();
            }
        });



        loadChildren();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (childrenList != null) {
            loadChildren();
        }
    }


    public void loadChildren() {
        if (progressBar == null || recyclerViewChildren == null || childrenList == null || childrenAdapter == null) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        textViewNoChildren.setVisibility(View.GONE);
        recyclerViewChildren.setVisibility(View.GONE);

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (progressBar == null || recyclerViewChildren == null || childrenList == null || childrenAdapter == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                childrenList.clear();
                if (children != null) {
                    childrenList.addAll(children);
                }
                childrenAdapter.notifyDataSetChanged();

                if (children == null || children.isEmpty()) {
                    if (textViewNoChildren != null) {
                        textViewNoChildren.setVisibility(View.VISIBLE);
                    }
                    recyclerViewChildren.setVisibility(View.GONE);
                } else {
                    if (textViewNoChildren != null) {
                        textViewNoChildren.setVisibility(View.GONE);
                    }
                    recyclerViewChildren.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progressBar == null || recyclerViewChildren == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                String errorMessage = e != null ? e.getMessage() : "Unknown error";
                Toast.makeText(getContext(), "Error loading children: " + errorMessage, Toast.LENGTH_SHORT).show();
                if (textViewNoChildren != null) {
                    textViewNoChildren.setVisibility(View.VISIBLE);
                }
                recyclerViewChildren.setVisibility(View.GONE);
            }
        });
    }

    private void showDeleteConfirmation(int position) {
        Map<String, Object> child = childrenList.get(position);
        String childName = (String) child.get("name");
        String childUid = (String) child.get("uid");

        new AlertDialog.Builder(getContext())
                .setTitle("Unlink Child")
                .setMessage("Are you sure you want to unlink " + childName + " from your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    unlinkChild(childUid);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void unlinkChild(String childUid) {
        progressBar.setVisibility(View.VISIBLE);
        ChildAccountManager.unlinkChildFromParent(childUid, new utils.DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Child unlinked successfully", Toast.LENGTH_SHORT).show();
                loadChildren();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error unlinking child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditPBsDialog() {
        if (childrenList == null || childrenList.isEmpty()) {
            Toast.makeText(getContext(), "No children linked yet", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_pbs, null);

        RecyclerView recyclerViewPBs = dialogView.findViewById(R.id.recyclerViewPBs);
        recyclerViewPBs.setLayoutManager(new LinearLayoutManager(getContext()));
        PBEditAdapter adapter = new PBEditAdapter(childrenList);
        recyclerViewPBs.setAdapter(adapter);

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Personal Best (PB) Values")
                .setView(dialogView)
                .setPositiveButton("Save All", (dialog, which) -> {
                    final int[] savedCount = {0};
                    final int[] errorCount = {0};
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        String childUid = adapter.getChildUid(i);
                        if (childUid != null) {
                            int pbValue = adapter.getPBValue(i, recyclerViewPBs);
                            if (pbValue > 0) {
                                final int finalI = i;
                                PBManager.setPB(childUid, pbValue, new utils.DatabaseManager.SuccessFailCallback() {
                                    @Override
                                    public void onSuccess() {
                                        savedCount[0]++;
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        errorCount[0]++;
                                    }
                                });
                            }
                        }
                    }
                    recyclerViewPBs.postDelayed(() -> {
                        if (errorCount[0] == 0) {
                            Toast.makeText(getContext(), "PB values saved successfully", Toast.LENGTH_SHORT).show();
                            loadChildren(); // Refresh the list
                        } else {
                            Toast.makeText(getContext(), "Some PB values could not be saved", Toast.LENGTH_SHORT).show();
                            loadChildren();
                        }
                    }, 500);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

