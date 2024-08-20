package org.albert.util;

import javax.swing.text.*;

public class WrapEditorKit extends StyledEditorKit
{
    private final ViewFactory defaultFactory = new WrapColumnFactory();

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
}

class WrapColumnFactory implements ViewFactory
{
    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            switch (kind)
            {
                case AbstractDocument.ContentElementName ->
                {
                    return new WrapLabelView(elem);
                }
                case AbstractDocument.ParagraphElementName ->
                {
                    return new ParagraphView(elem);
                }
                case AbstractDocument.SectionElementName ->
                {
                    return new BoxView(elem, View.Y_AXIS);
                }
                case StyleConstants.ComponentElementName ->
                {
                    return new ComponentView(elem);
                }
                case StyleConstants.IconElementName ->
                {
                    return new IconView(elem);
                }
            }
        }

        // default to text display
        return new LabelView(elem);
    }
}

class WrapLabelView extends LabelView {
    public WrapLabelView(Element elem) {
        super(elem);
    }

    @Override
    public float getMinimumSpan(int axis) {
        return switch (axis)
        {
            case View.X_AXIS -> 0;
            case View.Y_AXIS -> super.getMinimumSpan(axis);
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        };
    }
}
