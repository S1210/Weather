package ru.alex.weather.adpters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.alex.weather.R;
import ru.alex.weather.models.city.City;

public class RecyclerViewCitiesAdapter extends RecyclerView.Adapter<RecyclerViewCitiesAdapter.RecyclerViewHolder> {

    private List<City> cityList;
    private OnCityClickListener cityClickListener;

    public RecyclerViewCitiesAdapter(List<City> cityList, OnCityClickListener cityClickListener) {
        this.cityList = cityList;
        this.cityClickListener = cityClickListener;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_cities, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.bind(cityList.get(position));
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNameCity;
        private TextView tvCountryCity;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameCity = itemView.findViewById(R.id.tv_name_city);
            tvCountryCity = itemView.findViewById(R.id.tv_country_city);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    City city = cityList.get(getLayoutPosition());
                    cityClickListener.onCityClick(city);
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(City city) {
            tvNameCity.setText(city.getLocalizedName());
            tvCountryCity.setText(city.getCountry().getLocalizedName());
        }
    }

    public interface OnCityClickListener {
        void onCityClick(City city);
    }
}
