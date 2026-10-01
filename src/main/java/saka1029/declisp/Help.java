package saka1029.declisp;

public record Help(
    VT type,
    Symbol name,
    String args,
    String text) {

    @Override
    public String toString() {
        if (type == VT.variable)
            return "%s %s".formatted(type, name);
        else
            return "%s (%s%s%s) : %s".formatted(type, name, args.isEmpty() ? "" : " ", args, text);
    }

}
