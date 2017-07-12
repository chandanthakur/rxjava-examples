class Dish {
    private final int id;

    Dish(int id) {
        this.id = id;
        Utils.printVerbose("Dish", "Created: " + id);
    }

    Dish(Long id) {
        this.id = id.intValue();
        Utils.printVerbose("Dish", "Created: " + id);
    }

    public String toString() {
        return String.valueOf(id);
    }
}
