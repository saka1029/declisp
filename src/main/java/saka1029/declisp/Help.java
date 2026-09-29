package saka1029.declisp;

public class Help {

    public final VT type;
    public final Symbol name;
    public final String args;
    public final String text;

    public Help(VT type, Symbol name, String args, String text) {
        this.type = type;
        this.name = name;
        this.args = args;
        this.text = text;
    }

    @Override
    public String toString() {
        if (type == VT.variable)
            return "%s %s".formatted(type, name);
        else
            return "%s (%s%s%s) : %s".formatted(type, name, args.isEmpty() ? "" : " ", args, text);
    }

}
