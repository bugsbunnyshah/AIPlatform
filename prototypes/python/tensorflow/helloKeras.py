import keras
from keras import models, layers
# Define the model structure
model = models.Sequential()
model.add(layers.Dense(64, activation='relu', input_shape=(10,)))
...
model.add(layers.Dense(1, activation='sigmoid'))
# Compile and fit the model
model.compile(optimizer='rmsprop',loss='binary_crossentropy',
              metrics=[auc])
history = model.fit(x, y, epochs=100, batch_size=100,
                    validation_split = .2, verbose=0)
# Save the model in h5 format
model.save("games.h5")