package net.minecraft.client.settings;

public enum PointOfView {
    FIRST_PERSON(true, false),
    THIRD_PERSON_BACK(false, false),
    THIRD_PERSON_FRONT(false, true);

    private static final PointOfView[] POINT_OF_VIEWS = values();
    private final boolean firstPerson;
    private final boolean thirdPersonFront;

    PointOfView(boolean firstPerson, boolean thirdPersonFront) {
        this.firstPerson = firstPerson;
        this.thirdPersonFront = thirdPersonFront;
    }

    public boolean firstPerson() {
        return this.firstPerson;
    }

    public boolean thirdPersonFront() {
        return this.thirdPersonFront;
    }

    public PointOfView nextPointOfView() {
        return POINT_OF_VIEWS[(this.ordinal() + 1) % POINT_OF_VIEWS.length];
    }
}
