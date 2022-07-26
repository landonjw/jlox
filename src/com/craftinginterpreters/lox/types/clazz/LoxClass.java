package com.craftinginterpreters.lox.types.clazz;

import com.craftinginterpreters.lox.interpreter.Interpreter;
import com.craftinginterpreters.lox.types.LoxCallable;
import com.craftinginterpreters.lox.types.function.LoxFunction;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable
{
    public final String name;
    private final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    public LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods)
    {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    @Override
    public String toString()
    {
        return "<class " + name + ">";
    }

    @Override
    public int arity()
    {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments)
    {
        LoxInstance instance = new LoxInstance(this);

        LoxFunction initializer = findMethod("init");
        if (initializer != null)
        {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    public LoxFunction findMethod(String name)
    {
        if (methods.containsKey(name))
        {
            return methods.get(name);
        }

        if (superclass != null)
        {
            return superclass.findMethod(name);
        }

        return null;
    }
}