using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using CoffeeAPI.Models;

namespace CoffeeAPI.Data
{
    public class ApiDbContext : IdentityDbContext

    {
        public DbSet<CoffeeAPI.Models.Account> Accounts { get; set; }
        public DbSet<CoffeeAPI.Models.Order> Orders { get; set; }
        public DbSet<CoffeeAPI.Models.SubOrder> SubOrders { get; set; }
        public DbSet<CoffeeAPI.Models.Drink> Drinks { get; set; }

        public ApiDbContext(DbContextOptions<ApiDbContext> options) : base(options)
        {

        }

    }
}
