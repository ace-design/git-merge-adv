import math


class Person:
    species = "Homo sapiens"  # Class attribute

    def __init__(self, name, age):
        self.name = name  # Instance attribute
        self.age = age

    def say_hello(self):  # Instance method
        print(f"Hello, my name is {self.name}!")

    @classmethod
    def get_species(cls):  # Class method
        return cls.species

    @staticmethod
    def is_adult(age):  # Static method
        return age >= 18

    def __str__(self):  # Special method for string representation
        return f"Person: {self.name}, Age: {self.age}"





def c():
    if True:
        print("doing something")

def a():
    print("This is function1")


if __name__=="__main__":
    a()