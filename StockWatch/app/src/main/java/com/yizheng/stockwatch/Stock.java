package com.yizheng.stockwatch;

public class Stock implements Comparable<Stock>{

    private String stockSymbol, companyName;
    private double price, priceChange, changePercentage;

    public Stock(String stockSymbol, String companyName, double price, double priceChange, double changePercentage) {
        setStockSymbol(stockSymbol);
        setCompanyName(companyName);
        setPrice(price);
        setPriceChange(priceChange);
        setChangePercentage(changePercentage);
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getPrice() {
        return price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    @Override
    public int compareTo(Stock o) {
        return this.getStockSymbol().compareTo(o.getStockSymbol());
    }

//    @Override
//    public boolean equals(Object o){
//        if (o instanceof Stock) {
//            Stock s = (Stock) o;
//            return this.stockSymbol == s.stockSymbol;
//        }
//        return false;
//    }
}
