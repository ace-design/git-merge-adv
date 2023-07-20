import math
x = 12

y = 14

class Person:
    species = 'Homo sapiens'
    def __init__(self, name, age):
        self.name = name
        self.age = age
    
    
    def say_hello(self):
        print(f'Hello, my name is {self.name}!')
    
    
    @classmethod
    def get_species(cls):
        return cls.species
    
    
    @staticmethod
    def is_adult(age):
        return age >= 18
    
    
    def __str__(self):
        return f'Person: {self.name}, Age: {self.age}'
    
    

def c():
    if True:
        print('doing something')


def a():
    print('This is function1')

def b():
    for i in range(10):
        print('m')


if __name__ == '__main__':
    a()
