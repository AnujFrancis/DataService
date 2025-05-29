const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const fs = require('fs').promises;
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3002;
const DATA_FILE = path.join(__dirname, '../data/customers.json');

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Ensure data file exists
async function ensureDataFileExists() {
  try {
    await fs.access(DATA_FILE);
  } catch (error) {
    // Create directory if it doesn't exist
    try {
      await fs.mkdir(path.dirname(DATA_FILE), { recursive: true });
    } catch (err) {
      if (err.code !== 'EEXIST') throw err;
    }
    
    // Create empty data file
    await fs.writeFile(DATA_FILE, JSON.stringify({ customers: [] }));
    console.log('Created empty data file');
  }
}

// Helper to read data
async function readData() {
  await ensureDataFileExists();
  const data = await fs.readFile(DATA_FILE, 'utf8');
  return JSON.parse(data);
}

// Helper to write data
async function writeData(data) {
  await fs.writeFile(DATA_FILE, JSON.stringify(data, null, 2));
}

// Routes
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'healthy' });
});

// Create a new customer
app.post('/customers', async (req, res) => {
  try {
    const { name, alias, dob } = req.body;
    
    if (!name || !alias || !dob) {
      return res.status(400).json({ error: 'Name, alias, and date of birth are required' });
    }
    
    const data = await readData();
    
    // Check if customer with alias already exists
    const existingCustomer = data.customers.find(c => c.alias === alias);
    if (existingCustomer) {
      return res.status(409).json({ error: 'Customer with this alias already exists' });
    }
    
    const newCustomer = {
      id: Date.now().toString(),
      name,
      alias,
      dob,
      createdAt: new Date().toISOString()
    };
    
    data.customers.push(newCustomer);
    await writeData(data);
    
    res.status(201).json(newCustomer);
  } catch (error) {
    console.error('Error creating customer:', error);
    res.status(500).json({ error: 'Failed to create customer' });
  }
});

// Get all customers
app.get('/customers', async (req, res) => {
  try {
    const data = await readData();
    res.json(data.customers);
  } catch (error) {
    console.error('Error retrieving customers:', error);
    res.status(500).json({ error: 'Failed to retrieve customers' });
  }
});

// Get customer by ID, name, or alias
app.get('/customers/search', async (req, res) => {
  try {
    const { id, name, alias } = req.query;
    
    if (!id && !name && !alias) {
      return res.status(400).json({ error: 'At least one search parameter (id, name, or alias) is required' });
    }
    
    const data = await readData();
    let customer;
    
    if (id) {
      customer = data.customers.find(c => c.id === id);
    } else if (alias) {
      customer = data.customers.find(c => c.alias === alias);
    } else if (name) {
      customer = data.customers.find(c => c.name === name);
    }
    
    if (!customer) {
      return res.status(404).json({ error: 'Customer not found' });
    }
    
    res.json(customer);
  } catch (error) {
    console.error('Error searching for customer:', error);
    res.status(500).json({ error: 'Failed to search for customer' });
  }
});

// Update customer
app.put('/customers/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, alias, dob } = req.body;
    
    if (!name && !alias && !dob) {
      return res.status(400).json({ error: 'At least one field to update is required' });
    }
    
    const data = await readData();
    const customerIndex = data.customers.findIndex(c => c.id === id);
    
    if (customerIndex === -1) {
      return res.status(404).json({ error: 'Customer not found' });
    }
    
    // Check if alias is being updated and is already taken
    if (alias && alias !== data.customers[customerIndex].alias) {
      const aliasExists = data.customers.some((c, i) => i !== customerIndex && c.alias === alias);
      if (aliasExists) {
        return res.status(409).json({ error: 'Customer with this alias already exists' });
      }
    }
    
    // Update customer
    data.customers[customerIndex] = {
      ...data.customers[customerIndex],
      name: name || data.customers[customerIndex].name,
      alias: alias || data.customers[customerIndex].alias,
      dob: dob || data.customers[customerIndex].dob,
      updatedAt: new Date().toISOString()
    };
    
    await writeData(data);
    
    res.json(data.customers[customerIndex]);
  } catch (error) {
    console.error('Error updating customer:', error);
    res.status(500).json({ error: 'Failed to update customer' });
  }
});

// Delete customer
app.delete('/customers/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    const data = await readData();
    const customerIndex = data.customers.findIndex(c => c.id === id);
    
    if (customerIndex === -1) {
      return res.status(404).json({ error: 'Customer not found' });
    }
    
    const deletedCustomer = data.customers[customerIndex];
    data.customers.splice(customerIndex, 1);
    
    await writeData(data);
    
    res.json({ message: 'Customer deleted successfully', customer: deletedCustomer });
  } catch (error) {
    console.error('Error deleting customer:', error);
    res.status(500).json({ error: 'Failed to delete customer' });
  }
});

// Start server
async function startServer() {
  await ensureDataFileExists();
  app.listen(PORT, () => {
    console.log(`Data Service running on port ${PORT}`);
  });
}

startServer();

module.exports = app; // For testing
