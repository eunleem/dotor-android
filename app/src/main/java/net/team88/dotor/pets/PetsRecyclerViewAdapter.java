package net.team88.dotor.pets;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.team88.dotor.R;
import net.team88.dotor.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class PetsRecyclerViewAdapter extends RecyclerView.Adapter<PetsRecyclerViewAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardview;


        TextView textPetName;
        TextView textPetType;
        TextView textPetGender;
        TextView textPetAge;
        TextView textPetSize;
        Button buttonModify;

        public ViewHolder(View view) {
            super(view);

            this.cardview = (CardView) view.findViewById(R.id.cardview);

            this.textPetName = (TextView) view.findViewById(R.id.textPetName);
            this.textPetType = (TextView) view.findViewById(R.id.textPetType);
            this.textPetGender = (TextView) view.findViewById(R.id.textPetGender);
            this.textPetAge = (TextView) view.findViewById(R.id.textPetAge);
            this.textPetSize = (TextView) view.findViewById(R.id.textPetSize);

            this.buttonModify = (Button) view.findViewById(R.id.buttonModify);
        }
    }

    private Context context;

    public PetsRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_pet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LinkedHashMap<String, Pet> pets = MyPets.getInstance(context).getPets();
        ArrayList<Pet> petList = new ArrayList<>(pets.values());
        Collections.sort(petList);
        final Pet pet = petList.get(position);

        String petType = context.getString(R.string.puppy);
        if (pet.type == Pet.Type.CAT) {
            petType = context.getString(R.string.kitten);
        }

        String petGender = context.getString(R.string.male);
        if (pet.gender == Pet.Gender.FEMALE) {
            petGender = context.getString(R.string.female);
        }

        final String petAge = String.valueOf(Utils.getAge(pet.getBirthday())) + " " + context.getString(R.string.age_unit);

        String petSize;
        if (pet.size == Pet.Size.SMALL) {
            petSize = context.getString(R.string.pet_size_small);
        } else if (pet.size == Pet.Size.MEDIUM) {
            petSize = context.getString(R.string.pet_size_medium);
        } else if (pet.size == Pet.Size.LARGE) {
            petSize = context.getString(R.string.pet_size_large);
        } else if (pet.size == Pet.Size.XLARGE) {
            petSize = context.getString(R.string.pet_size_xlarge);
        } else {
            // ERROR
            petSize = "Unknown";
        }

        holder.textPetName.setText(pet.name);
        holder.textPetType.setText(petType);
        holder.textPetGender.setText(petGender);
        holder.textPetAge.setText(petAge);
        holder.textPetSize.setText(petSize);

        holder.buttonModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, PetEditActivity.class);
                intent.putExtra(PetEditActivity.KEY_MODE, PetEditActivity.VALUE_MODE_EDIT);
                intent.putExtra(PetEditActivity.KEY_PET_NAME, pet.name);
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return MyPets.getInstance(context).getPets().size();
    }

}
